package org.hesperides.core.domain.technos.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * permet de regrouper les envois de commandes.
 */
@Component
public class TechnoCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public TechnoCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String createTechno(Techno techno, User user) {
        return commandGateway.sendAndWait(new CreateTechnoCommand(techno, user));
    }

    public void addTemplate(String technoId, Template template, User user) {
        commandGateway.sendAndWait(new AddTemplateToTechnoCommand(technoId, template, user));
    }

    public void updateTemplate(String technoId, Template template, User user) {
        commandGateway.sendAndWait(new UpdateTechnoTemplateCommand(technoId, template, user));
    }

    public void deleteTechno(String technoId, User user) {
        commandGateway.sendAndWait(new DeleteTechnoCommand(technoId, user));
    }

    public void deleteTemplate(String technoId, String templateName, User user) {
        commandGateway.sendAndWait(new DeleteTechnoTemplateCommand(technoId, templateName, user));
    }
}
