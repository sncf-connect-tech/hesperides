package org.hesperides.tests.bdd.commons;

import com.mongodb.MongoClient;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.commons.spring.HasProfile.isProfileActive;
import static org.hesperides.commons.spring.SpringProfiles.INTEGRATION_TESTS;

@Configuration
@ContextConfiguration
public class AppliCleaner {

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

    public void tearDown() {
        if (!isProfileActive(INTEGRATION_TESTS)) {
            resetDatabases();
        }
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
