package org.hesperides.core.infrastructure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import lombok.Setter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

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
@ConfigurationProperties("projection-repository")
public class MongoProjectionRepositoryConfiguration {

    // On expose les noms des méthodes sous formes de strings pour pouvoir les utiliser comme "bean qualifiers" dans datamigration
    public final static String MONGO_CLIENT_URI_BEAN_NAME = "projectionMongoClientUri";
    public final static String MONGO_CLIENT_BEAN_NAME = "projectionMongoClient";
    public final static String MONGO_TEMPLATE_BEAN_NAME = "projectionMongoTemplate";

    @Setter
    @NotNull
    private String uri;

    @Bean
    public MongoClientURI projectionMongoClientUri() {
        return new MongoClientURI(uri);
    }

    @Bean
    @Primary // mongoDbFactory in org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration require it in datamigration
    public MongoClient projectionMongoClient(MongoClientURI projectionMongoClientUri) {
        return new MongoClient(projectionMongoClientUri);
    }

    @Bean({MONGO_TEMPLATE_BEAN_NAME, "mongoTemplate"}) // un Bean nommé mongoTemplate est requis pour les repos SpringData
    public MongoTemplate projectionMongoTemplate(MongoClient projectionMongoClient, MongoClientURI projectionMongoClientUri) {
        return new MongoTemplate(projectionMongoClient, projectionMongoClientUri.getDatabase());
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
