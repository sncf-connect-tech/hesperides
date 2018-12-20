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
        if (2 != (Integer) ((Document) indexPerName.get("_id_").get("collation")).get("strength")) {
            throw new RuntimeException("Collection ID is not case-insensitive");
        }
        if (2 != (Integer) ((Document) indexPerName.get("key_1").get("collation")).get("strength")) {
            throw new RuntimeException("Collection key is not case-insensitive");
        }
    }
}
