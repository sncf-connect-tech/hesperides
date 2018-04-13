package org.hesperides.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.technos.CreateTechnoCommand;
import org.hesperides.domain.technos.TechnoCreatedEvent;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

@NoArgsConstructor
@Slf4j
@Aggregate
class TechnoAggregate implements Serializable {
    @AggregateIdentifier
    private Techno.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    @CommandHandler
    public TechnoAggregate(CreateTechnoCommand command) {
        log.debug("Applying create techno command...");
        // Initialise le version_id à 1
        Techno techno = new Techno(command.getTechno().getKey(), 1L);
        apply(new TechnoCreatedEvent(techno, command.getUser()));
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void on(TechnoCreatedEvent event) {
        this.key = event.getTechno().getKey();

        log.debug("techno créée. (aggregate is live ? {})", isLive());
    }


}
