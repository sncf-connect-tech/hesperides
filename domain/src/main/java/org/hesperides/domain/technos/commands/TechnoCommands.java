package org.hesperides.domain.technos.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.CreateTechnoCommand;
import org.hesperides.domain.technos.entities.Techno;
import org.springframework.stereotype.Component;

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

    public Techno.Key createTechno(Techno techno, User user) {
        return commandGateway.sendAndWait(new CreateTechnoCommand(techno, user));
    }

}
