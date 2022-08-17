package org.hesperides.core.infrastructure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import lombok.Setter;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.bson.Document;
import org.hesperides.core.infrastructure.axon.SecureXStreamSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hesperides.commons.SpringProfiles.MONGO;

@Configuration
@Profile(MONGO)
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = "org.hesperides.core.infrastructure.mongo")
@Validated
@ConfigurationProperties("mongo")
public class MongoConfiguration {

    @Setter
    @NotNull
    private String uri;

    @Bean
    public ConnectionString connectionString() {
        return new ConnectionString(uri);
    }

    @Bean
    public MongoClient mongoClient(ConnectionString connectionString) {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient, ConnectionString connectionString) {
        return new MongoTemplate(mongoClient, connectionString.getDatabase());
    }

    @Bean
    @Primary
    public EventStorageEngine eventStorageEngine(MongoClient mongoClient, ConnectionString connectionString) {
        DefaultMongoTemplate mongoTemplate = DefaultMongoTemplate.builder()
                .mongoDatabase(mongoClient, connectionString.getDatabase())
                .build();
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

    public static void ensureCaseInsensitivity(MongoTemplate mongoTemplate, String collectionName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        List<Document> indexes = StreamSupport.stream(collection.listIndexes().spliterator(), false).collect(Collectors.toList());
        Map<String, Document> indexPerName = indexes.stream().collect(Collectors.toMap(i -> (String) i.get("name"), Function.identity()));
        if (2 != getCollationStrength(indexPerName, "_id_", collectionName)) {
            throw new RuntimeException("Index \"_id\" in collection " + collectionName + " is not case-insensitive");
        }
        if (2 != getCollationStrength(indexPerName, "key_1", collectionName)) {
            throw new RuntimeException("Index \"key\" in collection " + collectionName + " is not case-insensitive");
        }
    }

    private static Integer getCollationStrength(Map<String, Document> indexPerName, String indexName, String collectionName) {
        Document index = indexPerName.get(indexName);
        if (index == null) {
            throw new RuntimeException("No index named " + indexName + " exists for collection " + collectionName);
        }
        Document collation = (Document) index.get("collation");
        if (collation == null) {
            throw new RuntimeException("No collation found for index " + indexName + " in collection " + collectionName);
        }
        return (Integer) collation.get("strength");
    }
}

