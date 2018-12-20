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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile(MONGO)
@Configuration
@ConfigurationProperties("event-store")
@Validated
public class AxonMongoEventStoreConfiguration {

    // On expose les noms des méthodes sous formes de strings pour pouvoir les utiliser comme "bean qualifiers"
    public final static String MONGO_CLIENT_URI_BEAN_NAME = "axonMongoClientUri";
    public final static String MONGO_CLIENT_BEAN_NAME = "axonMongoClient";
    public final static String MONGO_TEMPLATE_BEAN_NAME = "axonMongoTemplate";

    @Setter
    @NotNull
    private String uri;

    @Bean
    public MongoClientURI axonMongoClientUri() {
        return new MongoClientURI(uri);
    }

    @Bean
    public MongoClient axonMongoClient(MongoClientURI axonMongoClientUri) {
        return new MongoClient(axonMongoClientUri);
    }

    @Bean
    public MongoTemplate axonMongoTemplate(MongoClient axonMongoClient, MongoClientURI axonMongoClientUri) {
        return new MongoTemplate(axonMongoClient, axonMongoClientUri.getDatabase());
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClient axonMongoClient, MongoClientURI axonMongoClientUri) {
        DefaultMongoTemplate mongoTemplate = new DefaultMongoTemplate(axonMongoClient, axonMongoClientUri.getDatabase());
        return new MongoEventStorageEngine(mongoTemplate);
    }
}

