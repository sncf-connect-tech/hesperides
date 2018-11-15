package org.hesperides.core.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.net.InetSocketAddress;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;

@Configuration
@Profile({FAKE_MONGO})
public class FakeAxonMongoEventStoreConfiguration {

    @Bean
    @Primary
    MongoClient mongoClient() {
        final MongoServer server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        final InetSocketAddress serverAddress = server.bind();

        return new MongoClient(new ServerAddress(serverAddress));
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClient client) {
        return new MongoEventStorageEngine(new DefaultMongoTemplate(client));
    }
}
