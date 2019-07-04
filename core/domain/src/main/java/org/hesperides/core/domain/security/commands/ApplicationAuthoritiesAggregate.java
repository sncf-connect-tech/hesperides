package org.hesperides.core.domain.security.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.authorizations.ApplicationAuthoritiesCreatedEvent;
import org.hesperides.core.domain.authorizations.ApplicationAuthoritiesUpdatedEvent;
import org.hesperides.core.domain.authorizations.CreateApplicationAuthoritiesCommand;
import org.hesperides.core.domain.authorizations.UpdateApplicationAuthoritiesCommand;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
class ApplicationAuthoritiesAggregate implements Serializable {

    @AggregateIdentifier
    private String id;
    private String applicationName;
    private Map<String, List<String>> authorities;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    public ApplicationAuthoritiesAggregate(CreateApplicationAuthoritiesCommand command) {
        String newUuid = UUID.randomUUID().toString();
        log.debug("ApplicationAuthoritiesAggregate constructor - id: %s - applicationName: %s - user: %s",
                newUuid, command.getApplicationAuthorities().getApplicationName(), command.getUser());
        apply(new ApplicationAuthoritiesCreatedEvent(newUuid, command.getApplicationAuthorities(), command.getUser().getName()));
    }


    @CommandHandler
    public void onUpdateApplicationAuthoritiesCommand(UpdateApplicationAuthoritiesCommand command) {
        log.debug("onUpdateApplicationAuthoritiesCommand - id: %s - applicationName: %s - user: %s",
                command.getId(), command.getApplicationAuthorities().getApplicationName(), command.getUser());
        apply(new ApplicationAuthoritiesUpdatedEvent(command.getId(), command.getApplicationAuthorities(), command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onApplicationAuthoritiesCreatedEvent(ApplicationAuthoritiesCreatedEvent event) {
        log.debug("onApplicationAuthoritiesCreatedEvent - id: %s - applicationName: %s - user: %s",
                event.getId(), event.getApplicationAuthorities().getApplicationName(), event.getUser());
        this.id = event.getId();
        this.applicationName = event.getApplicationAuthorities().getApplicationName();
        this.authorities = event.getApplicationAuthorities().getAuthorities();
    }

    @EventSourcingHandler
    public void onApplicationAuthoritiesUpdatedEvent(ApplicationAuthoritiesUpdatedEvent event) {
        log.debug("onApplicationAuthoritiesUpdatedEvent - id: %s - applicationName: %s - user: %s",
                event.getId(), event.getApplicationAuthorities().getApplicationName(), event.getUser());
        this.authorities = event.getApplicationAuthorities().getAuthorities();
    }
}
