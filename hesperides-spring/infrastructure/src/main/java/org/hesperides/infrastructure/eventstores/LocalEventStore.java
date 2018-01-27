package org.hesperides.infrastructure.eventstores;

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/**
 * in memory event storage if local profile is used.
 */
@Component
@Profile("local")
public class LocalEventStore extends EmbeddedEventStore {
    public LocalEventStore() {
        super(new InMemoryEventStorageEngine());
    }
}
