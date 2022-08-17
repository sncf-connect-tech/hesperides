package org.hesperides.test.mongo_integration.config;

import org.hesperides.core.infrastructure.axon.AxonConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Component
@PropertySource("application-test.yml")
@ComponentScan(basePackages = {"org.hesperides.test.bdd"})
@ComponentScan(basePackages = {"org.hesperides.core.infrastructure.mongo"},
        excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AxonConfiguration.class})})
@ComponentScan(basePackages = {"org.hesperides.commons"})
public class IntegTestConfig {
}
