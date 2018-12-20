package org.hesperides.tests.bdd;

import com.mongodb.MongoClient;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.commons.spring.HasProfile;
import org.hesperides.tests.bdd.commons.AppliCleaner;
import org.hesperides.tests.bdd.commons.TestContext;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.commons.spring.HasProfile.isProfileActive;
import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.INTEGRATION_TESTS;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"classpath:org.hesperides.tests.bdd"})
public class CucumberTests {

    @Profile("!"+INTEGRATION_TESTS)
    @SpringBootTest(classes = HesperidesSpringApplication.class, webEnvironment = RANDOM_PORT)
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
    @Configuration
    @ContextConfiguration
    public static class CucumberSpringBeanUnitTests {
        @Autowired
        private AppliCleaner appliCleaner;
        @After
        public void tearDown() {
            appliCleaner.tearDown();
        }
    }

//    @Profile(INTEGRATION_TESTS)
//    @Configuration
//    @ContextConfiguration
//    public static class CucumberSpringBeanIntegTests {
//        @Autowired
//        private AppliCleaner appliCleaner;
//        @After
//        public void tearDown() {
//            appliCleaner.tearDown();
//        }
//    }

    public static void main(String[] args) {
        JUnitCore.main("CucumberTests");
    }
}
