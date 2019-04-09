package org.hesperides.core.application.modules;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.modules.commands.ModuleCommands;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.modules.queries.TechnoModuleView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Ensemble des cas d'utilisation liés à l'agrégat Module
 */
@Component
public class ModuleUseCases {

    private final ModuleCommands commands;
    private final ModuleQueries queries;
    private final TechnoQueries technoQueries;

    @Autowired
    public ModuleUseCases(ModuleCommands commands, ModuleQueries queries, TechnoQueries technoQueries) {
        this.commands = commands;
        this.queries = queries;
        this.technoQueries = technoQueries;
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
        if (queries.moduleExists(module.getKey())) {
            throw new DuplicateModuleException(module.getKey());
        }
        verifyTechnosExistence(module.getTechnos());
        return commands.createModule(module, user);
    }

    public String createWorkingCopyFrom(TemplateContainer.Key existingModuleKey, TemplateContainer.Key newModuleKey, User user) {

        if (queries.moduleExists(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }

        Optional<ModuleView> optionalModuleView = queries.getOptionalModule(existingModuleKey);
        if (!optionalModuleView.isPresent()) {
            throw new ModuleNotFoundException(existingModuleKey);
        }

        Module existingModule = optionalModuleView.get().toDomainInstance();
        Module newModule = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        return commands.createModule(newModule, user);
    }

    public void updateModuleTechnos(Module module, User user) {
        Optional<String> optionalModuleId = queries.getOptionalModuleId(module.getKey());
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        verifyTechnosExistence(module.getTechnos());
        commands.updateModuleTechnos(optionalModuleId.get(), module, user);
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
        Optional<String> optionalModuleId = queries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        commands.deleteModule(optionalModuleId.get(), user);
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
        Optional<String> optionalModuleId = queries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        commands.createTemplateInWorkingCopy(optionalModuleId.get(), moduleKey, template, user);
    }

    public void updateTemplateInWorkingCopy(TemplateContainer.Key moduleKey, Template template, User user) {
        Optional<String> optionalModuleId = queries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        commands.updateTemplateInWorkingCopy(optionalModuleId.get(), moduleKey, template, user);
    }

    public void deleteTemplate(TemplateContainer.Key moduleKey, String templateName, User user) {
        Optional<String> optionalModuleId = queries.getOptionalModuleId(moduleKey);
        if (!optionalModuleId.isPresent()) {
            throw new ModuleNotFoundException(moduleKey);
        }
        commands.deleteTemplate(optionalModuleId.get(), moduleKey, templateName, user);
    }

    public Optional<ModuleView> getModule(TemplateContainer.Key moduleKey) {
        return queries.getOptionalModule(moduleKey);
    }

    public Optional<ModuleView> getModule(String moduleId) {
        return queries.getOptionalModule(moduleId);
    }

    public List<String> getModulesName() {
        return queries.getModulesName();
    }

    public List<String> getModuleVersions(String moduleName) {
        return queries.getModuleVersions(moduleName);
    }

    public List<String> getModuleTypes(String moduleName, String moduleVersion) {
        return queries.getModuleTypes(moduleName, moduleVersion);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key moduleKey, String templateName) {
        return queries.getTemplate(moduleKey, templateName);
    }

    public List<ModuleView> search(String input) {
        return queries.search(input);
    }

    public Optional<ModuleView> searchSingle(String input) {
        String[] values = input.split(" ");
        String name = values.length > 0 ? values[0] : "";
        String version = values.length > 1 ? values[1] : "";
        String workingCopy = values.length > 2 ? values[2] : "";

        Optional<ModuleView> moduleView;
        if (StringUtils.isNotEmpty(workingCopy)) {
            boolean isWorkingCopy = !"false".equalsIgnoreCase(workingCopy);
            moduleView = queries.getOptionalModule(new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy)));
        } else {
            List<ModuleView> moduleViews = queries.search(input);
            moduleView = moduleViews.size() > 0 ? Optional.of(moduleViews.get(0)) : Optional.empty();
        }
        return moduleView;
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key moduleKey) {
        return queries.getTemplates(moduleKey);
    }

    public ModuleView createRelease(String moduleName, String moduleVersion, String releaseVersion, User user) {

        String version = StringUtils.isEmpty(releaseVersion) ? moduleVersion : releaseVersion;
        TemplateContainer.Key newModuleKey = new Module.Key(moduleName, version, TemplateContainer.VersionType.release);
        if (queries.moduleExists(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }

        TemplateContainer.Key existingModuleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        Optional<ModuleView> optionalModuleView = queries.getOptionalModule(existingModuleKey);
        if (!optionalModuleView.isPresent()) {
            throw new ModuleNotFoundException(existingModuleKey);
        }

        Module existingModule = optionalModuleView.get().toDomainInstance();
        Module moduleRelease = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        commands.createModule(moduleRelease, user);
        return queries.getOptionalModule(newModuleKey).get();
    }

    public List<AbstractPropertyView> getPropertiesModel(TemplateContainer.Key moduleKey) {
        if (!queries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        return queries.getPropertiesModel(moduleKey);
    }

    public List<TechnoModuleView> getModulesUsingTechno(Techno.Key technoKey) {
        Optional<String> optTechnoId = technoQueries.getOptionalTechnoId(technoKey);
        if (!optTechnoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        return queries.getModulesUsingTechno(optTechnoId.get());
    }
}
