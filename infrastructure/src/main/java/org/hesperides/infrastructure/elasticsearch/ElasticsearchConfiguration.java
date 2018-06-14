package org.hesperides.infrastructure.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.hesperides.domain.framework.Profiles.ELASTICSEARCH;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableElasticsearchRepositories(basePackages = "org.hesperides.infrastructure.elasticsearch")
@Profile(ELASTICSEARCH)
@Import({
        ElasticsearchAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class
})
public class ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String indexName;

    @Bean
    public String indexName() {
        return indexName;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventStorageEngine inmemoryEventStorageEngine() {
        return new InMemoryEventStorageEngine();
    }
}
