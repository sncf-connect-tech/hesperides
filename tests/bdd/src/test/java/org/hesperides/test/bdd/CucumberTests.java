package org.hesperides.test.bdd;

import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.test.bdd.commons.DbCleaner;
import org.hesperides.test.bdd.commons.TestContextCleaner;
import org.hesperides.test.bdd.config.TestConfig;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd"},
        tags = {"~@require-real-mongo", "~@require-real-ad"}) // comma in tag = OR, comma between tags = AND
public class CucumberTests {

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        JUnitCore.main("CucumberTests");
    }

    @SpringBootTest(classes = {HesperidesSpringApplication.class, TestConfig.class}, webEnvironment = RANDOM_PORT)
    @ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
    @Configuration
    @ContextConfiguration
    @EnableTransactionManagement(proxyTargetClass = true) // avoids: BeanNotOfRequiredTypeException
    public static class SpringUnitTests {
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
