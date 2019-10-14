package org.hesperides.test.bdd.commons;

import com.mongodb.MongoClient;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

import static org.hesperides.core.infrastructure.mongo.Collections.*;

@Configuration
public class DbCleaner {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoClient mongoClient;

    public void wipeOutCollections() {
        for (String collection : Arrays.asList(MODULE, PLATFORM, TECHNO, APPLICATION_DIRECTORY_GROUPS)) {
            mongoTemplate.getCollection(collection).drop();
        }
        new DefaultMongoTemplate(mongoClient).eventCollection().drop();
        new DefaultMongoTemplate(mongoClient).snapshotCollection().drop();
    }
}
