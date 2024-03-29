package org.hesperides.core.domain.security.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.security.ApplicationDirectoryGroupsCreatedEvent;
import org.hesperides.core.domain.security.ApplicationDirectoryGroupsUpdatedEvent;
import org.hesperides.core.domain.security.CreateApplicationDirectoryGroupsCommand;
import org.hesperides.core.domain.security.UpdateApplicationDirectoryGroupsCommand;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
class ApplicationDirectoryGroupsAggregate implements Serializable {

    @AggregateIdentifier
    private String id;
    private String applicationName;
    private List<String> directoryGroupDNs;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    public ApplicationDirectoryGroupsAggregate(CreateApplicationDirectoryGroupsCommand command) {
        String newUuid = UUID.randomUUID().toString();
        log.debug("ApplicationDirectoryGroupsAggregate constructor - id: %s - applicationName: %s - user: %s",
                newUuid, command.getApplicationDirectoryGroups().getApplicationName(), command.getUser());
        apply(new ApplicationDirectoryGroupsCreatedEvent(newUuid, command.getApplicationDirectoryGroups(), command.getUser().getName()));
    }


    @CommandHandler
    public void onUpdateApplicationDirectoryGroupsCommand(UpdateApplicationDirectoryGroupsCommand command) {
        log.debug("onUpdateApplicationDirectoryGroupsCommand - id: %s - applicationName: %s - user: %s",
                command.getId(), command.getApplicationDirectoryGroups().getApplicationName(), command.getUser());
        apply(new ApplicationDirectoryGroupsUpdatedEvent(command.getId(), command.getApplicationDirectoryGroups(), command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onApplicationDirectoryGroupsCreatedEvent(ApplicationDirectoryGroupsCreatedEvent event) {
        log.debug("onApplicationDirectoryGroupsCreatedEvent - id: %s - applicationName: %s - user: %s",
                event.getId(), event.getApplicationDirectoryGroups().getApplicationName(), event.getUser());
        this.id = event.getId();
        this.applicationName = event.getApplicationDirectoryGroups().getApplicationName();
        this.directoryGroupDNs = event.getApplicationDirectoryGroups().getDirectoryGroupDNs();
    }

    @EventSourcingHandler
    public void onApplicationDirectoryGroupsUpdatedEvent(ApplicationDirectoryGroupsUpdatedEvent event) {
        log.debug("onApplicationDirectoryGroupsUpdatedEvent - id: %s - applicationName: %s - user: %s",
                event.getId(), event.getApplicationDirectoryGroups().getApplicationName(), event.getUser());
        this.directoryGroupDNs = event.getApplicationDirectoryGroups().getDirectoryGroupDNs();
    }
}
