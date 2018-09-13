package org.hesperides.tests.bdd;

import com.mongodb.MongoClient;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources")
public class CucumberTests {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoClient client;

    @After
    public void tearDown() {
        mongoTemplate.getDb().dropDatabase();
        new DefaultMongoTemplate(client).eventCollection().drop();
    }
}
