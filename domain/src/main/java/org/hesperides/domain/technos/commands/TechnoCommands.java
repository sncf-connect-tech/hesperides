package org.hesperides.domain.technos.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.AddTemplateToTechnoCommand;
import org.hesperides.domain.technos.CreateTechnoCommand;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * permet de regrouper les envois de commandes.
 */
@Component
public class TechnoCommands {

    private final CommandGateway commandGateway;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TechnoCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public Techno.Key createTechno(TemplateContainer.Key technoKey, User user) {
        Techno techno = new Techno(technoKey, Collections.emptyList());
        return commandGateway.sendAndWait(new CreateTechnoCommand(techno, user));
    }

    public void addTemplate(TemplateContainer.Key technoKey, Template template, User user) {
        commandGateway.sendAndWait(new AddTemplateToTechnoCommand(technoKey, template, user));
    }
}
