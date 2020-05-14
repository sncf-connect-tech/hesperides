package org.hesperides.test.mongo_integration;

import io.cucumber.java.Before;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.hesperides.test.bdd.configuration.TestContextCleaner;
import org.hesperides.test.bdd.configuration.TestDatabaseCleaner;
import org.hesperides.test.mongo_integration.config.IntegTestConfig;
import org.hesperides.test.mongo_integration.config.IntegTestHttpConfig;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hesperides.commons.SpringProfiles.*;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = true,
        plugin = "pretty",
        features = "../bdd/src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd", "classpath:org.hesperides.test.mongo_integration"},
        tags = "not @require-real-ad")
public class CucumberMongoIntegTests {

    public static void main(String[] args) {
        JUnitCore.main("CucumberMongoIntegTests");
    }

    @ActiveProfiles(profiles = {MONGO, NOLDAP, TEST})
    @ContextConfiguration(classes = {IntegTestConfig.class, IntegTestHttpConfig.class})
    public static class SpringIntegTests {
        @Autowired
        private TestContextCleaner testContextCleaner;
        @Autowired
        private TestDatabaseCleaner testDatabaseCleaner;

        @Before
        public void cleanUp() {
            testContextCleaner.reset();
            testDatabaseCleaner.wipeOutCollections();
        }
    }
}
