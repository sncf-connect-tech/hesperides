/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.domain.workshopproperties.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.CreateWorkshopPropertyCommand;
import org.hesperides.domain.UpdateWorkshopPropertyCommand;
import org.hesperides.domain.WorkshopPropertyCreatedEvent;
import org.hesperides.domain.WorkshopPropertyUpdatedEvent;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;

import java.io.Serializable;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@Aggregate
@NoArgsConstructor
public class WorkshopPropertyAggregate implements Serializable {

    @AggregateIdentifier
    private String key;

    @CommandHandler
    public WorkshopPropertyAggregate(CreateWorkshopPropertyCommand command) {

        WorkshopProperty processedWorkshopProperty = new WorkshopProperty(
                command.getWorkshopProperty().getKey(),
                command.getWorkshopProperty().getValue(),
                command.getWorkshopProperty().getKey() + command.getWorkshopProperty().getValue()
        );

        apply(new WorkshopPropertyCreatedEvent(processedWorkshopProperty, command.getUser()));
    }

    @CommandHandler
    public void handle(UpdateWorkshopPropertyCommand command) {

        WorkshopProperty processedWorkshopProperty = new WorkshopProperty(
                command.getWorkshopProperty().getKey(),
                command.getWorkshopProperty().getValue(),
                command.getWorkshopProperty().getKey() + command.getWorkshopProperty().getValue()
        );

        apply(new WorkshopPropertyUpdatedEvent(processedWorkshopProperty, command.getUser()));
    }

    @EventSourcingHandler
    public void on(WorkshopPropertyCreatedEvent event) {
        this.key = event.getWorkshopProperty().getKey();
        log.debug("Workshop property created");
    }

    @EventSourcingHandler
    public void on(WorkshopPropertyUpdatedEvent event) {
        this.key = event.getWorkshopProperty().getKey();
        log.debug("Workshop property updated");
    }
}
