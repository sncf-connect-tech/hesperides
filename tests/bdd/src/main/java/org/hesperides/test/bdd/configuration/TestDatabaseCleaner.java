package org.hesperides.test.bdd.configuration;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class TestDatabaseCleaner {
    @Autowired
    private MongoTemplate mongoTemplate;

    public void wipeOutCollections() {
        for (String collection : mongoTemplate.getCollectionNames()) {
            // Ne pas utiliser `.drop()` sur les collections pour ne pas supprimer
            // les collations créées dans mongo_create_collections.js
            mongoTemplate.getCollection(collection).deleteMany(new Document());
        }
    }
}
