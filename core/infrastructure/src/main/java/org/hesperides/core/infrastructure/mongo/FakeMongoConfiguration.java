package org.hesperides.core.infrastructure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.hesperides.core.infrastructure.axon.SecureXStreamSerializer;
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

    @Bean(destroyMethod = "shutdown")
    public MongoServer mongoServer() {
        MemoryBackend memoryBackend = new MemoryBackend();
        MongoServer mongoServer = new MongoServer(memoryBackend);
        mongoServer.bind();
        return mongoServer;
    }

    @Bean
    public String mongoUri(MongoServer mongoServer) {
        InetSocketAddress serverAddress = mongoServer.getLocalAddress();
        return "mongodb://" + serverAddress.getHostName() + ":" + serverAddress.getPort() + "/" + MONGO_DB_NAME;
    }

    @Bean
    public MongoClient mongoClient(String mongoUri) {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, MONGO_DB_NAME);
    }

    @Bean
    public EventStorageEngine eventStorageEngine(MongoClient mongoClient) {
        DefaultMongoTemplate mongoTemplate = DefaultMongoTemplate.builder().mongoDatabase(mongoClient, MONGO_DB_NAME).build();
        return MongoEventStorageEngine.builder()
                .eventSerializer(SecureXStreamSerializer.get())
                .snapshotSerializer(SecureXStreamSerializer.get())
                .mongoTemplate(mongoTemplate)
                .build();
    }

    @Bean
    public EmbeddedEventStore embeddedEventStore(EventStorageEngine eventStorageEngine) {
        return EmbeddedEventStore.builder()
                .storageEngine(eventStorageEngine)
                .build();
    }
}
