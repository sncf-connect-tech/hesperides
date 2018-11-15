package org.hesperides.tests.bdd;

import com.mongodb.MongoClient;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.hesperides.HesperidesSpringApplication;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"classpath:org.hesperides.tests.bdd"})
public class CucumberTests {

    @Configuration
    @SpringBootTest(classes = HesperidesSpringApplication.class, webEnvironment = RANDOM_PORT)
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
    @ContextConfiguration
    public static class CucumberSpringBean {

        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private MongoClient client;

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private TechnoBuilder technoBuilder;
        @Autowired
        private TemplateBuilder templateBuilder;
        @Autowired
        private PropertyBuilder propertyBuilder;
        @Autowired
        private ModelBuilder modelBuilder;
        @Autowired
        private ModuleBuilder moduleBuilder;
        @Autowired
        private PlatformBuilder platformBuilder;

        @After
        public void tearDown() {
            resetDatabases();
            resetRestTemplateAuthHeader();
            resetBuilders();
        }

        private void resetDatabases() {
            mongoTemplate.getDb().drop();
            new DefaultMongoTemplate(client).eventCollection().drop();
        }

        private void resetRestTemplateAuthHeader() {
            if (restTemplate.getInterceptors().contains(TestContext.BASIC_AUTH_INTERCEPTOR)) {
                restTemplate.getInterceptors().remove(TestContext.BASIC_AUTH_INTERCEPTOR);
            }
        }

        private void resetBuilders() {
            templateBuilder.reset();
            technoBuilder.reset();
            propertyBuilder.reset();
            modelBuilder.reset();
            moduleBuilder.reset();
            platformBuilder.reset();
            platformBuilder.resetPlatforms();
        }
    }

    public static void main(String[] args) {
        JUnitCore.main("CucumberTests");
    }
}
