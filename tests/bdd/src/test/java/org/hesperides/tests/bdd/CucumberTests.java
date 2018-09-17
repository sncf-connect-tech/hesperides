package org.hesperides.tests.bdd;

import com.mongodb.MongoClient;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.hesperides.HesperidesSpringApplication;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"classpath:org.hesperides.tests.bdd"})
public class CucumberTests {

    @Configuration
    @SpringBootTest(classes = HesperidesSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
    @ContextConfiguration
    public static class CucumberSpringBean {

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

    public static void main(String[] args) {
        JUnitCore.main("CucumberTests");
    }
}
