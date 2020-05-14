package org.hesperides.test.bdd;

import io.cucumber.java.Before;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.test.bdd.configuration.TestContextCleaner;
import org.hesperides.test.bdd.configuration.TestDatabaseCleaner;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.hesperides.commons.SpringProfiles.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        strict = true,
        plugin = "pretty",
        features = "src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd"},
        tags = {"not @require-real-mongo and not @require-real-ad"})
public class CucumberTests {

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        JUnitCore.main("CucumberTests");
    }

    @SpringBootTest(classes = {HesperidesSpringApplication.class}, webEnvironment = RANDOM_PORT)
    // Ce dernier profil active la prise en compte du application-test.yml
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP, TEST})
    @ContextConfiguration
    @EnableTransactionManagement(proxyTargetClass = true) // avoids: BeanNotOfRequiredTypeException
    public static class SpringUnitTests {
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
