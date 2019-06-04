package org.hesperides.test.integration;

import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.hesperides.test.bdd.commons.DbCleaner;
import org.hesperides.test.bdd.commons.TestContextCleaner;
import org.hesperides.test.integration.config.IntegTestConfig;
import org.hesperides.test.integration.config.IntegTestHttpConfig;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "../bdd/src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd", "classpath:org.hesperides.test.integration"})
public class CucumberIntegTests {

    @ActiveProfiles(profiles = {MONGO, NOLDAP, "test"}) // "test" => pick up application-test.yml*-
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

    public static void main(String[] args) {
        JUnitCore.main("CucumberIntegTests");
    }
}
