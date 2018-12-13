package org.hesperides.core.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Setter;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.validation.constraints.NotNull;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile(MONGO)
@Configuration
@ConfigurationProperties("event-store")
public class AxonMongoEventStoreConfiguration {

    @Setter
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
    public EventStorageEngine eventStore(MongoClient axonMongoClient, MongoClientURI axonMongoClientUri) {
        DefaultMongoTemplate mongoTemplate = new DefaultMongoTemplate(axonMongoClient, axonMongoClientUri.getDatabase());
        return new MongoEventStorageEngine(mongoTemplate);
    }
}

