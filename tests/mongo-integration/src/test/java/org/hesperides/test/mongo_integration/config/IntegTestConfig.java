package org.hesperides.test.mongo_integration.config;

import org.hesperides.core.infrastructure.mongo.eventstores.SnapshotConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@PropertySource("application-integ-test.properties")
@ComponentScan(basePackages = {"org.springframework.boot.actuate.autoconfigure.health"}) // provides HealthAggregator Bean
@ComponentScan(basePackages = {"org.hesperides.test.bdd"})
@ComponentScan(basePackages = {"org.hesperides.core.infrastructure.mongo"},
        excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SnapshotConfiguration.class})})
public class IntegTestConfig {
}
