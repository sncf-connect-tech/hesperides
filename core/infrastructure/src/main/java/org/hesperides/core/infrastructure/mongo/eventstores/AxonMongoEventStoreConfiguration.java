package org.hesperides.core.infrastructure.mongo.eventstores;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.mongo.DefaultMongoTemplate;
import org.axonframework.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO})
@Configuration
@Getter
@Setter
@Validated
@ConfigurationProperties("event-store")
public class AxonMongoEventStoreConfiguration {

    private String uri;
    private Integer eventsCountToTriggerSnapshot;

    @Bean
    public MongoClient axonMongoClient(MongoClientURI axonMongoClientUri) {
        return new MongoClient(axonMongoClientUri);
    }

    @Bean
    public MongoClientURI axonMongoClientUri() {
        return new MongoClientURI(uri);
    }

    @Bean
    @Primary
    public EventStorageEngine eventStore(MongoClientURI axonMongoClientUri) {
        DefaultMongoTemplate mongoTemplate = new DefaultMongoTemplate(axonMongoClient(axonMongoClientUri), axonMongoClientUri.getDatabase());
        return new MongoEventStorageEngine(mongoTemplate);
    }

    @Bean
    public SpringAggregateSnapshotter snapshotter(ParameterResolverFactory parameterResolverFactory,
                                                  EventStore eventStore,
                                                  TransactionManager transactionManager) {

        // https://docs.axoniq.io/reference-guide/v/3.3/part-iii-infrastructure-components/repository-and-event-store#creating-a-snapshot
        // (...) Therefore, it is recommended to run the snapshotter in a different thread (...)
        Executor executor = Executors.newSingleThreadExecutor();
        return new SpringAggregateSnapshotter(eventStore, parameterResolverFactory, executor, transactionManager);
    }

    @Bean
    public EventCountSnapshotTriggerDefinition snapshotTrigger(SpringAggregateSnapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, snapshotThreshold);
    }
}

