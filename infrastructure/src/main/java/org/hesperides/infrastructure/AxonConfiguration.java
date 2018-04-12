package org.hesperides.infrastructure;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventStorageEngine inmemoryEventStorageEngine() {
        return new InMemoryEventStorageEngine();
    }

}
