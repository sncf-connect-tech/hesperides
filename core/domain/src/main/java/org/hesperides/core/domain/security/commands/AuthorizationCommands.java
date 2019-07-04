package org.hesperides.core.domain.security.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public AuthorizationCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
}
