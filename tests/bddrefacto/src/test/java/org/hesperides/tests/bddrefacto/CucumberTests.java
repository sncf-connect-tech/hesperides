package org.hesperides.tests.bddrefacto;

import com.mongodb.MongoClient;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
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
        glue = {"classpath:org.hesperides.tests.bddrefacto"})
public class CucumberTests {

    @Configuration
    @SpringBootTest(classes = HesperidesSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
    @ContextConfiguration
//    @DirtiesContext
    public static class CucumberSpringBean {

        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private MongoClient client;
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

        @After
        public void tearDown() {
            mongoTemplate.getDb().dropDatabase();
            new DefaultMongoTemplate(client).eventCollection().drop();
            templateBuilder.reset();
            technoBuilder.reset();
            propertyBuilder.reset();
            modelBuilder.reset();
            moduleBuilder.reset();
        }
    }

    public static void main(String[] args) {
        JUnitCore.main("CucumberTests");
    }
}
