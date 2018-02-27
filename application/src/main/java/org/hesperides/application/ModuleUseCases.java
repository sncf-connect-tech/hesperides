package org.hesperides.application;

import org.hesperides.application.exceptions.DuplicateModuleException;
import org.hesperides.application.exceptions.ModuleNotFoundException;
import org.hesperides.domain.modules.commands.ModuleCommands;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.queries.ModuleQueries;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.modules.queries.TemplateView;
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
     * @param newModuleKey
     * @return
     */
    public Module.Key createWorkingCopy(Module.Key newModuleKey) {
        if (queries.moduleExist(newModuleKey)) {
            throw new DuplicateModuleException(newModuleKey);
        }
        return commands.createModule(newModuleKey);
    }

    public Module.Key updateWorkingCopy(Module.Key moduleKey) {
        if (!queries.moduleExist(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        return commands.updateModule(moduleKey);
    }

    /**
     * créer un template dans un module déjà existant.
     * <p>
     * Si le module n'existe pas, une erreur sera levée par Axon (l'aggregat n'est pas trouvé)
     * <p>
     * Si le template existe déjà dans le module, c'est le module lui-même qui levera une exception.
     *
     * @param key
     * @param template
     */
    public void createTemplateInWorkingCopy(Module.Key key, Template template) {
        commands.createTemplateInWorkingCopy(key, template);
    }

    public void updateTemplateInWorkingCopy(Module.Key key, Template template) {
        commands.updateTemplateInWorkingCopy(key, template);
    }

    public void deleteTemplate(Module.Key key, String templateName) {
        commands.deleteTemplate(key, templateName);
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

    public Module.Key createWorkingCopyFrom(Module.Key from, Module.Key key) {
        throw new IllegalArgumentException("TODO"); //TODO
    }
}
