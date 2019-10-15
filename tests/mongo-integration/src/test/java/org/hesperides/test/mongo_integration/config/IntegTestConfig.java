package org.hesperides.test.mongo_integration.config;

import org.hesperides.core.infrastructure.axon.AxonSnapshotConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@PropertySource("application-test.yml")
@ComponentScan(basePackages = {"org.springframework.boot.actuate.autoconfigure.health"})
// provides HealthAggregator Bean
@ComponentScan(basePackages = {"org.hesperides.test.bdd"})
@ComponentScan(basePackages = {"org.hesperides.core.infrastructure.mongo"},
        excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AxonSnapshotConfiguration.class})})
@ComponentScan(basePackages = {"org.hesperides.commons"})
public class IntegTestConfig {
}
