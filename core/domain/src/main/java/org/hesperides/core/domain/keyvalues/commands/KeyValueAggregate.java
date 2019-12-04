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
package org.hesperides.core.domain.keyvalues.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.keyvalues.*;
import org.hesperides.core.domain.keyvalues.entities.KeyValue;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@Aggregate
public class KeyValueAggregate implements Serializable {

    @AggregateIdentifier
    private String id;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    public KeyValueAggregate(CreateKeyValueCommand command) {
        String id = UUID.randomUUID().toString();
        KeyValue keyValue = command
                .getKeyValue()
                .concatKeyValue();
        log.info("Creating key-value");
        apply(new KeyValueCreatedEvent(id, keyValue, command.getUser().getName()));
    }

    @CommandHandler
    public void onUpdateKeyValueCommand(UpdateKeyValueCommand command) {
        KeyValue keyValue = command
                .getKeyValue()
                .concatKeyValue();
        log.info("Updating key-value");
        apply(new KeyValueUpdatedEvent(command.getId(), keyValue, command.getUser().getName()));
    }

    @CommandHandler
    public void onDeleteKeyValueCommand(DeleteKeyValueCommand command) {
        log.info("Deleting key-value");
        apply(new KeyValueDeletedEvent(command.getId(), command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onKeyValueCreatedEvent(KeyValueCreatedEvent event) {
        log.info("Key-value created");
        this.id = event.getId();
    }

    @EventSourcingHandler
    public void onKeyValuUpdatedEvent(KeyValueUpdatedEvent event) {
        log.info("Key-value updated");
    }

    @EventSourcingHandler
    public void onKeyValueDeletedEvent(KeyValueDeletedEvent event) {
        log.info("Key-value deleted");
    }
}
