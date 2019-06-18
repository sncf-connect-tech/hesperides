package org.hesperides.test.mongo_integration;

import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.hesperides.test.bdd.commons.DbCleaner;
import org.hesperides.test.bdd.commons.TestContextCleaner;
import org.hesperides.test.mongo_integration.config.IntegTestConfig;
import org.hesperides.test.mongo_integration.config.IntegTestHttpConfig;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "../bdd/src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd", "classpath:org.hesperides.test.mongo_integration"},
        tags = "~@require-real-ad")
public class CucumberMongoIntegTests {

    public static void main(String[] args) {
        JUnitCore.main("CucumberMongoIntegTests");
    }

    @ActiveProfiles(profiles = {MONGO, NOLDAP})
    @ContextConfiguration(classes = {IntegTestConfig.class, IntegTestHttpConfig.class})
    @Configuration
    public static class SpringIntegTests {
        @Autowired
        private TestContextCleaner testContextCleaner;
        @Autowired
        private DbCleaner dbCleaner;

        @Before
        public void cleanUp() {
            testContextCleaner.reset();
            dbCleaner.wipeOutCollections();
        }
    }
}
