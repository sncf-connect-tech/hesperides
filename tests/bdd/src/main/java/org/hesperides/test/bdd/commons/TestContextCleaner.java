package org.hesperides.test.bdd.commons;

import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Configuration
@ContextConfiguration
public class TestContextCleaner {

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
    private ModuleHistory moduleHistory;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public void reset() {
        resetRestTemplateAuthHeader();
        resetBuilders();
    }

    private void resetRestTemplateAuthHeader() {
        // On supprime le BasicAuthenticationInterceptor précédement configuré:
        restTemplate.setInterceptors(Collections.emptyList());
    }

    private void resetBuilders() {
        templateBuilder.reset();
        technoBuilder.reset();
        propertyBuilder.reset();
        modelBuilder.reset();
        moduleBuilder.reset();
        moduleHistory.reset();
        platformBuilder.reset();
        platformHistory.reset();
    }
}
