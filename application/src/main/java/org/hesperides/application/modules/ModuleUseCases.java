package org.hesperides.application.modules;

import org.hesperides.domain.exceptions.OutOfDateVersionException;
import org.hesperides.domain.modules.commands.ModuleCommands;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.domain.modules.queries.ModuleQueries;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.domain.technos.queries.TechnoQueries;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    public TemplateContainer.Key createWorkingCopy(Module module, User user) {
        if (queries.moduleExists(module.getKey())) {
            throw new DuplicateModuleException(module.getKey());
        }
        verifyTechnos(module.getTechnos());
        return commands.createModule(module, user);
    }

    public void updateModuleTechnos(Module module, User user) {
        Optional<ModuleView> moduleView = queries.getModule(module.getKey());
        if (!moduleView.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        if (!moduleView.get().getVersionId().equals(module.getVersionId())) {
            throw new OutOfDateVersionException(moduleView.get().getVersionId(), module.getVersionId());
        }
        verifyTechnos(module.getTechnos());
        commands.updateModuleTechnos(module, user);
    }

    private void verifyTechnos(List<Techno> technos) {
        if (technos != null) {
            for (Techno techno : technos) {
                if (!technoQueries.technoExists(techno.getKey())) {
                    throw new TechnoNotFoundException(techno.getKey());
                }
            }
        }
    }

    public void deleteModule(TemplateContainer.Key moduleKey, User user) {
        if (!queries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        commands.deleteModule(moduleKey, user);
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
        commands.createTemplateInWorkingCopy(moduleKey, template, user);
    }

    public void updateTemplateInWorkingCopy(TemplateContainer.Key moduleKey, Template template, User user) {
        commands.updateTemplateInWorkingCopy(moduleKey, template, user);
    }

    public void deleteTemplate(TemplateContainer.Key moduleKey, String templateName, User user) {
        commands.deleteTemplate(moduleKey, templateName, user);
    }

    public Optional<ModuleView> getModule(TemplateContainer.Key moduleKey) {
        return queries.getModule(moduleKey);
    }

    public List<String> getModulesNames() {
        return queries.getModulesNames();
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

    public ModuleView createWorkingCopyFrom(TemplateContainer.Key existingModuleKey, TemplateContainer.Key newModuleKey, User user) {

        if (queries.moduleExists(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }

        Optional<ModuleView> moduleView = queries.getModule(existingModuleKey);
        if (!moduleView.isPresent()) {
            throw new ModuleNotFoundException(existingModuleKey);
        }

        Module existingModule = moduleView.get().toDomainInstance();
        Module newModule = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        commands.createModule(newModule, user);
        return queries.getModule(newModuleKey).get();
    }

    public List<ModuleView> search(String input) {
        return queries.search(input);
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
        Optional<ModuleView> moduleView = queries.getModule(existingModuleKey);
        if (!moduleView.isPresent()) {
            throw new ModuleNotFoundException(existingModuleKey);
        }

        Module existingModule = moduleView.get().toDomainInstance();
        Module moduleRelease = new Module(newModuleKey, existingModule.getTemplates(), existingModule.getTechnos(), -1L);

        commands.createModule(moduleRelease, user);
        return queries.getModule(newModuleKey).get();
    }

    public List<AbstractPropertyView> getProperties(TemplateContainer.Key moduleKey) {
        if (!queries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        return queries.getProperties(moduleKey);
    }
}
