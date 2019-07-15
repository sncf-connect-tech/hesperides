package org.hesperides.core.domain.security.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.authorizations.CreateApplicationDirectoryGroupsCommand;
import org.hesperides.core.domain.authorizations.UpdateApplicationDirectoryGroupsCommand;
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDirectoryGroupsCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public ApplicationDirectoryGroupsCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public void createApplicationDirectoryGroups(ApplicationDirectoryGroups applicationDirectoryGroups, User user) {
        commandGateway.sendAndWait(new CreateApplicationDirectoryGroupsCommand(applicationDirectoryGroups, user));
    }

    public void updateApplicationDirectoryGroups(String id, ApplicationDirectoryGroups applicationDirectoryGroups, User user) {
        commandGateway.sendAndWait(new UpdateApplicationDirectoryGroupsCommand(id, applicationDirectoryGroups, user));
    }
}
