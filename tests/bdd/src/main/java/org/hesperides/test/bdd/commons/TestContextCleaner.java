package org.hesperides.test.bdd.commons;

import org.hesperides.test.bdd.applications.ApplicationDirectoryGroupsBuilder;
import org.hesperides.test.bdd.files.FileBuilder;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
@ContextConfiguration
public class TestContextCleaner {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;
    @Autowired
    private FileBuilder fileBuilder;
    @Autowired
    private ApplicationDirectoryGroupsBuilder applicationDirectoryGroupsBuilder;

    public void reset() {
        resetRestTemplateAuthHeader();
        resetBuilders();
    }

    private void resetRestTemplateAuthHeader() {
        // On supprime le BasicAuthenticationInterceptor précédement configuré :
        restTemplate.setInterceptors(Collections.emptyList());
    }

    private void resetBuilders() {
        templateBuilder.reset();
        technoBuilder.reset();
        technoHistory.reset();
        propertyBuilder.reset();
        moduleBuilder.reset();
        moduleHistory.reset();
        platformBuilder.reset();
        platformHistory.reset();
        deployedModuleBuilder.reset();
        instanceBuilder.reset();
        fileBuilder.reset();
        applicationDirectoryGroupsBuilder.reset();
    }
}
