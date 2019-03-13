package org.hesperides.core.domain.modules.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
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

    public String createModule(Module module, User user) {
        return commandGateway.sendAndWait(new CreateModuleCommand(module, user));
    }

    public void updateModuleTechnos(String moduleId, Module module, User user) {
        commandGateway.sendAndWait(new UpdateModuleTechnosCommand(moduleId, module, user));
    }

    public void deleteModule(String moduleId, User user) {
        commandGateway.sendAndWait(new DeleteModuleCommand(moduleId, user));
    }

    public void createTemplateInWorkingCopy(String moduleId, TemplateContainer.Key moduleKey, Template template, User user) {
        commandGateway.sendAndWait(new CreateTemplateCommand(moduleId, moduleKey, template, user));
    }

    public void updateTemplateInWorkingCopy(String moduleId, TemplateContainer.Key moduleKey, Template template, User user) {
        commandGateway.sendAndWait(new UpdateTemplateCommand(moduleId, moduleKey, template, user));
    }

    public void deleteTemplate(String moduleId, TemplateContainer.Key moduleKey, String templateName, User user) {
        commandGateway.sendAndWait(new DeleteTemplateCommand(moduleId, moduleKey, templateName, user));
    }
}
