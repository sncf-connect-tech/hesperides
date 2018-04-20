package org.hesperides.application.modules;

import org.hesperides.domain.exceptions.OutOfDateVersionException;
import org.hesperides.domain.modules.commands.ModuleCommands;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.domain.modules.queries.ModuleQueries;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
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

    @Autowired
    public ModuleUseCases(ModuleCommands commands, ModuleQueries queries) {
        this.commands = commands;
        this.queries = queries;
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
    public Module.Key createWorkingCopy(Module module, User user) {
        if (queries.moduleExist(module.getKey())) {
            throw new DuplicateModuleException(module.getKey());
        }
        return commands.createModule(module, user);
    }

    public Module.Key updateWorkingCopy(Module module, User user) {
        Optional<ModuleView> moduleView = queries.getModule(module.getKey());
        if (!moduleView.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        if (!moduleView.get().getVersionId().equals(module.getVersionId())) {
            throw new OutOfDateVersionException(moduleView.get().getVersionId(), module.getVersionId());
        }
        return commands.updateModule(module, user);
    }

    public void deleteWorkingCopy(Module module, User user) {
        Optional<ModuleView> optionalModuleView = queries.getModule(module.getKey());
        if (!optionalModuleView.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        commands.deleteModule(module, user);
    }

    public void deleteRelease(Module module, User user) {
        Optional<ModuleView> optionalModuleView = queries.getModule(module.getKey());
        if (!optionalModuleView.isPresent()) {
            throw new ModuleNotFoundException(module.getKey());
        }
        commands.deleteModule(module, user);
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
    public void createTemplateInWorkingCopy(Module.Key moduleKey, Template template, User user) {
        commands.createTemplateInWorkingCopy(moduleKey, template, user);
    }

    public void updateTemplateInWorkingCopy(Module.Key moduleKey, Template template, User user) {
        commands.updateTemplateInWorkingCopy(moduleKey, template, user);
    }

    public void deleteTemplate(Module.Key moduleKey, String templateName, User user) {
        commands.deleteTemplate(moduleKey, templateName, user);
    }

    public Optional<ModuleView> getModule(Module.Key moduleKey) {
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

    public Optional<TemplateView> getTemplate(Module.Key moduleKey, String templateName) {
        return queries.getTemplate(moduleKey, templateName);
    }

    public Module.Key createWorkingCopyFrom(Module.Key existingModuleKey, Module.Key newModuleKey) {
        throw new IllegalArgumentException("TODO"); //TODO
    }
}
