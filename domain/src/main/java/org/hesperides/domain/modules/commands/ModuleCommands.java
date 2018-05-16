package org.hesperides.domain.modules.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * permet de regrouper les envois de commandes.
 */
@Component
public class ModuleCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public ModuleCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public Module.Key createModule(Module module, User user) {
        return commandGateway.sendAndWait(new CreateModuleCommand(module, user));
    }

    public void updateModuleTechnos(Module module, User user) {
        commandGateway.sendAndWait(new UpdateModuleTechnosCommand(module.getKey(), module.getTechnos(), module.getVersionId(), user));
    }

    public void deleteModule(TemplateContainer.Key moduleKey, User user) {
        commandGateway.sendAndWait(new DeleteModuleCommand(moduleKey, user));
    }

    public void createTemplateInWorkingCopy(Module.Key key, Template template, User user) {
        commandGateway.sendAndWait(new CreateTemplateCommand(key, template, user));
    }

    public void updateTemplateInWorkingCopy(Module.Key key, Template template, User user) {
        commandGateway.sendAndWait(new UpdateTemplateCommand(key, template, user));
    }

    public void deleteTemplate(Module.Key key, String templateName, User user) {
        commandGateway.sendAndWait(new DeleteTemplateCommand(key, templateName, user));
    }
}
