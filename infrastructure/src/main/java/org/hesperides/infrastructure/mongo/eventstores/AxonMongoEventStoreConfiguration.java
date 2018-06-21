package org.hesperides.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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

import java.util.Collections;

import static org.hesperides.domain.framework.Profiles.MONGO;

@Configuration
@Profile({MONGO})
@Getter
@Setter
@Validated
@ConfigurationProperties("event_store")
public class AxonMongoEventStoreConfiguration {
    @NotNull
    private String host;
    @NotNull
    private String port;
    private String database;
    private String username;
    private String password;

    @Bean
    public MongoClient eventStoreMongoClient() {
        if (!username.isEmpty()) {
            return new MongoClient(new ServerAddress(host, Integer.parseInt(port)), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
        } else {
            return new MongoClient(host, Integer.parseInt(port));
        }
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore() {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(eventStoreMongoClient(), database));
    }


}
