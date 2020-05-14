package org.hesperides.test.bdd.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class TestDatabaseCleaner {
    @Autowired
    private MongoTemplate mongoTemplate;

    public void wipeOutCollections() {
        for (String collection : mongoTemplate.getCollectionNames()) {
            mongoTemplate.getCollection(collection).drop();
        }
    }
}
