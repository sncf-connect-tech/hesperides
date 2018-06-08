package org.hesperides.infrastructure.mongo.eventstores;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

import static org.hesperides.domain.Profiles.*;

@Configuration
@Profile({MONGO})
public class AxonMongoEventStoreConfiguration {
    @Value("${hesperides.eventstore.database}")
    private String database;

    @Value("${hesperides.eventstore.host}")
    private String host;

    @Value("${hesperides.eventstore.port}")
    private int port;

    @Value("${hesperides.eventstore.username}")
    private String username;

    @Value("${hesperides.eventstore.password}")
    private String password;

    @Bean
    public MongoClient mongo() throws Exception {
        return new MongoClient(new ServerAddress(host, port), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore() throws Exception {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(mongo(),database));
    }


}
