package org.hesperides.core.infrastructure.mongo;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@Getter
@Setter
@Validated
@ConfigurationProperties("mongo-search-options")
public class MongoSearchOptions {

    @NotNull
    private Integer moduleSearchMaxResults = 10;

    @NotNull
    private Integer technoSearchMaxResults = 10;

    public static void ensureCaseInsensitivity(MongoTemplate mongoTemplate, String collectionName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        List<Document> indexes = StreamSupport.stream(collection.listIndexes().spliterator(), false).collect(Collectors.toList());
        Map<String, Document> indexPerName = indexes.stream().collect(Collectors.toMap(i -> (String)i.get("name"), Function.identity()));
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
