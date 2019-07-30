package org.hesperides.test.bdd.commons;

import com.mongodb.MongoClient;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.bson.Document;
import org.hesperides.core.infrastructure.mongo.MongoProjectionRepositoryConfiguration;
import org.hesperides.core.infrastructure.mongo.eventstores.AxonMongoEventStoreConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

import static org.hesperides.core.infrastructure.Collections.*;

@Configuration
public class DbCleaner {

    @Autowired
    @Qualifier(MongoProjectionRepositoryConfiguration.MONGO_TEMPLATE_BEAN_NAME)
    private MongoTemplate mongoTemplateProjectionRepo;
    @Autowired
    @Qualifier(AxonMongoEventStoreConfiguration.MONGO_CLIENT_BEAN_NAME)
    private MongoClient mongoClientEventStore;

    public void wipeOutCollections() {
        for (String collection : Arrays.asList(MODULE, PLATFORM, TECHNO, APPLICATION_DIRECTORY_GROUPS)) {
            mongoTemplateProjectionRepo.getCollection(collection).deleteMany(new Document());
        }
        new DefaultMongoTemplate(mongoClientEventStore).eventCollection().deleteMany(new Document());
        new DefaultMongoTemplate(mongoClientEventStore).snapshotCollection().deleteMany(new Document());
    }
}
