package org.hesperides.test.activedirectory_integration.config;

import com.google.gson.Gson;
import org.hesperides.test.bdd.configuration.CustomRestTemplate;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class TestConfig {
    @Bean
    public DefaultUriBuilderFactory defaultUriBuilderFactory() {
        return new DefaultUriBuilderFactory();
    }

    @Bean
    public CustomRestTemplate buildRestTemplate(Environment environment, Gson gson, DefaultUriBuilderFactory defaultUriBuilderFactory) {
        return new CustomRestTemplate(gson, new LocalHostUriTemplateHandler(environment, "http", defaultUriBuilderFactory));
    }
}
