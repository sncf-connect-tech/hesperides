package org.hesperides.core.infrastructure.mongo.eventstores;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;

@Configuration
@Profile({FAKE_MONGO})
public class FakeAxonMongoEventStoreConfiguration {

    @Bean
    @Primary
    MongoClient mongoClient() {
        return new Fongo("fake_mongo").getMongo();
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClient client) {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(client));
    }
}
