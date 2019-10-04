package org.hesperides.core.application.modules;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.modules.commands.ModuleCommands;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.core.domain.modules.exceptions.ModuleHasWorkingcopyTechnoException;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.exceptions.ModuleUsedByPlatformsException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.swap;
import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.VersionType.release;
import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.VersionType.workingcopy;

/**
 * Ensemble des cas d'utilisation liés à l'agrégat Module
 */
@Component
public class ModuleUseCases {

    private static final int DEFAULT_NB_SEARCH_RESULTS = 10;

    private final ModuleCommands moduleCommands;
    private final ModuleQueries moduleQueries;
    private final TechnoQueries technoQueries;
    private final PlatformQueries platformQueries;

    @Autowired
    public ModuleUseCases(ModuleCommands moduleCommands, ModuleQueries moduleQueries, TechnoQueries technoQueries, PlatformQueries platformQueries) {
        this.moduleCommands = moduleCommands;
        this.moduleQueries = moduleQueries;
        this.technoQueries = technoQueries;
        this.platformQueries = platformQueries;
    }

    /**
     * creer une working copy, vérifie que le module n'existe pas déjà.
     * <p>
     * On test si le module existe déjà ou pas dans cette couche car un aggregat (un module)
     * n'as pas accès aux autres aggregats.
     *
     * @param module
     * @param user
     * @return
     */
    public String createWorkingCopy(Module module, User user) {
        if (moduleQueries.moduleExists(module.getKey())) {
            throw new DuplicateModuleException(module.getKey());
        }
        verifyTechnosExistence(module.getTechnos());
        return moduleCommands.createModule(module, user);
    }

    public String createWorkingCopyFrom(TemplateContainer.Key existingModuleKey, TemplateContainer.Key newModuleKey, User user) {

        if (moduleQueries.moduleExists(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }

        Optional<ModuleView> optionalModuleView = moduleQueries.getOptionalModule(existingModuleKey);
        if (!optionalModuleView.isPresent()) {
            throw new ModuleNotFoundException(existingModuleKey);
        }

        Module existingModule = optionalModuleView.get().toDomainInstance();
        Module newModule = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        return moduleCommands.createModule(newModule, user);
    }

    public void updateModuleTechnos(Module module, User user) {
        Optional<String> optionalModuleId = moduleQueries.getOptionalModuleId(module.getKey());
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        verifyTechnosExistence(module.getTechnos());
        moduleCommands.updateModuleTechnos(optionalModuleId.get(), module, user);
    }

    private void verifyTechnosExistence(List<Techno> technos) {
        if (technos != null) {
            for (Techno techno : technos) {
                if (!technoQueries.technoExists(techno.getKey())) {
                    throw new TechnoNotFoundException(techno.getKey());
                }
            }
        }
    }

    public void deleteModule(TemplateContainer.Key moduleKey, User user) {
        Optional<String> optionalModuleId = moduleQueries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }

        List<ModulePlatformView> modulePlatformViews = platformQueries.getPlatformsUsingModule((Module.Key) moduleKey);
        if (!CollectionUtils.isEmpty(modulePlatformViews)) {
            throw new ModuleUsedByPlatformsException(moduleKey, modulePlatformViews);
        }

        moduleCommands.deleteModule(optionalModuleId.get(), user);
    }

    /**
     * créer un template dans un module déjà existant.
     * <p>
     * Si le module n'existe pas, une erreur sera levée par Axon (l'aggregat n'est pas trouvé)
     * <p>
     * Si le template existe déjà dans le module, c'est le module lui-même qui levera une exception.
     *
     * @param moduleKey
     * @param template
     * @param user
     */
    public void createTemplateInWorkingCopy(TemplateContainer.Key moduleKey, Template template, User user) {
        Optional<String> optionalModuleId = moduleQueries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        moduleCommands.createTemplateInWorkingCopy(optionalModuleId.get(), moduleKey, template, user);
    }

    public void updateTemplateInWorkingCopy(TemplateContainer.Key moduleKey, Template template, User user) {
        Optional<String> optionalModuleId = moduleQueries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        moduleCommands.updateTemplateInWorkingCopy(optionalModuleId.get(), moduleKey, template, user);
    }

    public void deleteTemplate(TemplateContainer.Key moduleKey, String templateName, User user) {
        Optional<String> optionalModuleId = moduleQueries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        moduleCommands.deleteTemplate(optionalModuleId.get(), moduleKey, templateName, user);
    }

    public Optional<ModuleView> getModule(TemplateContainer.Key moduleKey) {
        return moduleQueries.getOptionalModule(moduleKey);
    }

    public Optional<ModuleView> getModule(String moduleId) {
        return moduleQueries.getOptionalModule(moduleId);
    }

    public List<String> getModulesName() {
        return moduleQueries.getModulesName();
    }

    public List<String> getModuleVersions(String moduleName) {
        return moduleQueries.getModuleVersions(moduleName);
    }

    public List<String> getModuleTypes(String moduleName, String moduleVersion) {
        return moduleQueries.getModuleTypes(moduleName, moduleVersion);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key moduleKey, String templateName) {
        return moduleQueries.getTemplate(moduleKey, templateName);
    }

    public List<ModuleView> search(String input, Integer providedSize) {
        int size = providedSize != null && providedSize > 0 ? providedSize : DEFAULT_NB_SEARCH_RESULTS;
        List<ModuleView> matchingModules = moduleQueries.search(input, size);
        // On insère un éventuel module ayant exactement ce numéro de version en 1ère position - cf. issue #595 & BDD correspondant :
        Optional<ModuleView> exactVersionMatch = input.contains(" ") ? searchSingle(input) : Optional.empty();
        if (exactVersionMatch.isPresent()) {
            ModuleView exactMatchingModule = exactVersionMatch.get();
            if (!swapExactMatchingModuleInFirstPosition(matchingModules, module -> module.getKey().equals(exactMatchingModule.getKey()))) {
                matchingModules.set(0, exactMatchingModule);
            }
        } else { // Cas où il n'y a pas de "exact version match", on remonte en 1èere position tout de même un éventuel "exact name match"
            // Remarques sur la ligne suivante :
            // - "input" est garanti "notBlank" par le controller appelant
            // - l'appel à .split renvoie "input" si aucun espace n'est trouvé dans la chaine de caractères
            String searchedModuleName = input.split(" ")[0];
            swapExactMatchingModuleInFirstPosition(matchingModules, module -> module.getName().equals(searchedModuleName));
        }
        return matchingModules;
    }

    private static boolean swapExactMatchingModuleInFirstPosition(List<ModuleView> modules, Predicate<ModuleView> isExactModuleMatch) {
        OptionalInt matchingModuleIndex = IntStream.range(0, modules.size())
                .filter(index -> isExactModuleMatch.test(modules.get(index)))
                .findAny();
        if (matchingModuleIndex.isPresent() && matchingModuleIndex.getAsInt() > 0) {
            // Si un module trouvé a exactement le même nom que recherché, mais n'est pas en 1ère position, on l'y place :
            swap(modules, 0, matchingModuleIndex.getAsInt());
        }
        return matchingModuleIndex.isPresent();
    }

    public Optional<ModuleView> searchSingle(String input) {
        Optional<ModuleView> moduleView;
        // Si une version est passée en input on tente de récupérer le module correspondant.
        // Sinon on effectue une recherche classique et on récupère la premier résultat.
        Optional<Module.Key> moduleKey = Module.Key.fromSearchInput(input);
        if (moduleKey.isPresent()) {
            moduleView = moduleQueries.getOptionalModule(moduleKey.get());
        } else {
            List<ModuleView> moduleViews = moduleQueries.search(input, DEFAULT_NB_SEARCH_RESULTS);
            moduleView = moduleViews.size() > 0 ? Optional.of(moduleViews.get(0)) : Optional.empty();
        }
        return moduleView;
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key moduleKey) {
        return moduleQueries.getTemplates(moduleKey);
    }

    public ModuleView createRelease(String moduleName, String moduleVersion, String releaseVersion, User user) {

        TemplateContainer.Key existingModuleKey = new Module.Key(moduleName, moduleVersion, workingcopy);
        Module existingModule = moduleQueries
                .getOptionalModule(existingModuleKey)
                .orElseThrow(() -> new ModuleNotFoundException(existingModuleKey))
                .toDomainInstance();

        List<Techno> workingcopyTechnos = existingModule.getTechnos().stream()
                .filter(techno -> techno.getKey().isWorkingCopy())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(workingcopyTechnos)) {
            throw new ModuleHasWorkingcopyTechnoException(existingModuleKey, workingcopyTechnos);
        }

        String version = StringUtils.isEmpty(releaseVersion) ? moduleVersion : releaseVersion;
        TemplateContainer.Key newModuleKey = new Module.Key(moduleName, version, release);
        if (moduleQueries.moduleExists(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }

        Module moduleRelease = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        moduleCommands.createModule(moduleRelease, user);
        return moduleQueries.getOptionalModule(newModuleKey).get();
    }

    public List<AbstractPropertyView> getPropertiesModel(TemplateContainer.Key moduleKey) {
        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        return moduleQueries.getPropertiesModel(moduleKey);
    }

    public List<TemplateContainerKeyView> getModulesUsingTechno(Techno.Key technoKey) {
        Optional<String> optTechnoId = technoQueries.getOptionalTechnoId(technoKey);
        if (!optTechnoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        return moduleQueries.getModulesUsingTechno(optTechnoId.get());
    }
}
