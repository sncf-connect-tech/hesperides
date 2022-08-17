package org.hesperides.test.activedirectory_integration;

import io.cucumber.java.Before;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.test.bdd.configuration.TestContextCleaner;
import org.hesperides.test.bdd.configuration.TestDatabaseCleaner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = "pretty",
        features = "../bdd/src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd", "classpath:org.hesperides.test.activedirectory_integration"},
        tags = "@require-real-ad or @auth-related"
)
public class CucumberActiveDirectoryIntegTests {

    @SpringBootTest(classes = {HesperidesSpringApplication.class}, webEnvironment = RANDOM_PORT)
    @ActiveProfiles(profiles = {"fake_mongo", "ldap"})
    @CucumberContextConfiguration
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
