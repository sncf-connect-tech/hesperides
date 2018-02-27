package org.hesperides.domain.modules.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.springframework.stereotype.Component;

/**
 * permet de regrouper les envois de commandes.
 */
@Component
public class ModuleCommands {

    private final CommandGateway commandGateway;

    public ModuleCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public Module.Key createModule(Module.Key key) {
        return commandGateway.sendAndWait(new CreateModuleCommand(key));
    }

    public Module.Key updateModule(Module.Key key) {
        return commandGateway.sendAndWait(new UpdateModuleCommand(key));
    }

    public void createTemplateInWorkingCopy(Module.Key key, Template template) {
        commandGateway.sendAndWait(new CreateTemplateCommand(key, template));
    }

    public void updateTemplateInWorkingCopy(Module.Key key, Template template) {
        commandGateway.sendAndWait(new UpdateTemplateCommand(key, template));
    }

    public void deleteTemplate(Module.Key key, String templateName) {
        commandGateway.sendAndWait(new DeleteTemplateCommand(key, templateName));
    }
}
