package org.hesperides.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.hesperides.domain.Profiles.EMBEDDED_MONGO;
import static org.hesperides.domain.Profiles.FAKE_MONGO;

@Configuration
@Profile({EMBEDDED_MONGO, FAKE_MONGO})
public class AxonMongoEmbeddedEventStoreConfiguration {
    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClient client) {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(client));
    }
}
