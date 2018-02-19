package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.application.exceptions.DuplicateModuleException;
import org.hesperides.domain.modules.commands.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.queries.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
     *
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

    /**
     * créer un template dans un module déjà existant.
     *
     * Si le module n'existe pas, une erreur sera levée par Axon (l'aggregat n'est pas trouvé)
     *
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

    public Optional<TemplateView> getTemplate(Module.Key moduleKey, String templateName) {
        return queries.getTemplate(moduleKey, templateName);
    }

    public Module.Key createWorkingCopyFrom(Module.Key from, Module.Key key) {
        throw new IllegalArgumentException("TODO"); //TODO
    }
}
