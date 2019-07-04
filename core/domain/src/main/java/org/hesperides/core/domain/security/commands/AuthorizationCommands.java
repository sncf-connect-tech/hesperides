package org.hesperides.core.domain.security.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.authorizations.CreateApplicationAuthoritiesCommand;
import org.hesperides.core.domain.authorizations.UpdateApplicationAuthoritiesCommand;
import org.hesperides.core.domain.security.entities.ApplicationAuthorities;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public AuthorizationCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public void createApplicationAuthorities(ApplicationAuthorities applicationAuthorities, User user) {
        commandGateway.sendAndWait(new CreateApplicationAuthoritiesCommand(applicationAuthorities, user));
    }

    public void updateApplicationAuthorities(String id, ApplicationAuthorities applicationAuthorities, User user) {
        commandGateway.sendAndWait(new UpdateApplicationAuthoritiesCommand(id, applicationAuthorities, user));
    }
}
