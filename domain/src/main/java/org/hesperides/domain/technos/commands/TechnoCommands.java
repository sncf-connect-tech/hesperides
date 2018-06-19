package org.hesperides.domain.technos.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.*;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
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

    public TemplateContainer.Key createTechno(Techno techno, User user) {
        return commandGateway.sendAndWait(new CreateTechnoCommand(techno, user));
    }

    public void addTemplate(TemplateContainer.Key technoKey, Template template, User user) {
        commandGateway.sendAndWait(new AddTemplateToTechnoCommand(technoKey, template, user));
    }

    public void updateTemplate(TemplateContainer.Key key, Template template, User user) {
        commandGateway.sendAndWait(new UpdateTechnoTemplateCommand(key, template, user));
    }

    public void deleteTechno(TemplateContainer.Key technoKey, User user) {
        commandGateway.sendAndWait(new DeleteTechnoCommand(technoKey, user));
    }

    public void deleteTemplate(TemplateContainer.Key key, String templateName, User user) {
        commandGateway.sendAndWait(new DeleteTechnoTemplateCommand(key, templateName, user));
    }
}
