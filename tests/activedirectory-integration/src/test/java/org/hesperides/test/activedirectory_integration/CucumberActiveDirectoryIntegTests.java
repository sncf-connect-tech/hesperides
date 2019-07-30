package org.hesperides.test.activedirectory_integration;

import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.hesperides.HesperidesSpringApplication;
import org.hesperides.test.activedirectory_integration.config.TestConfig;
import org.hesperides.test.bdd.commons.DbCleaner;
import org.hesperides.test.bdd.commons.TestContextCleaner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "../bdd/src/test/resources",
        glue = {"classpath:org.hesperides.test.bdd", "classpath:org.hesperides.test.activedirectory_integration"},
        tags = {"@require-real-ad,@auth-related"})
public class CucumberActiveDirectoryIntegTests {

    @SpringBootTest(classes = {HesperidesSpringApplication.class, TestConfig.class}, webEnvironment = RANDOM_PORT)
    @ActiveProfiles(profiles = {"fake_mongo", "ldap"})
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
