package org.hesperides.core.infrastructure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.net.InetSocketAddress;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;

@Configuration
@Profile(FAKE_MONGO)
public class FakeMongoConfiguration {

    private static final String MONGO_DB_NAME = "fake_database";

    @Bean(destroyMethod = "close")
    public MongoClient mongoClient() {
        MemoryBackend backend = new MemoryBackend();
        backend.setVersion(3, 2, 0);
        MongoServer server = new MongoServer(backend);
        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();
        return new MongoClient(new ServerAddress(serverAddress));
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, MONGO_DB_NAME);
    }

    @Bean
    public EventStorageEngine eventStorageEngine(MongoClient mongoClient) {
        DefaultMongoTemplate axonMongoTemplate = new DefaultMongoTemplate(mongoClient, MONGO_DB_NAME);
        return new MongoEventStorageEngine(axonMongoTemplate);
    }
}
