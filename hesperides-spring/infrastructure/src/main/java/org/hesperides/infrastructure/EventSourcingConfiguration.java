package org.hesperides.infrastructure;

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventSourcingConfiguration {


    @Bean
    EventStore eventStore() {
        return new EmbeddedEventStore(storageEngine());
    }

    @Bean
    EventStorageEngine storageEngine() {
        return new InMemoryEventStorageEngine();
    }
}
