package org.hesperides.core.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Configuration
@Profile({MONGO})
@Getter
@Setter
@Validated
@ConfigurationProperties("event_store")
public class AxonMongoEventStoreConfiguration {

    @NotNull
    private String uri;

    @Bean
    public MongoClient axonMongoClient(MongoClientURI axonMongoClientUri) {
        return new MongoClient(axonMongoClientUri);
    }

    @Bean
    public MongoClientURI axonMongoClientUri() {
        return new MongoClientURI(uri);
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClientURI axonMongoClientUri) {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(axonMongoClient(axonMongoClientUri), axonMongoClientUri.getDatabase()));
    }
}
