package org.hesperides.infrastructure.jpa.eventstores;

import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

import static org.hesperides.domain.framework.Profiles.JPA;

@Configuration
@Profile(JPA)
public class AxonJpaEventStoreConfiguration {

    @Bean
    public SpringDataSourceConnectionProvider springDataSourceConnectionProvider(DataSource dataSource) {
        return new SpringDataSourceConnectionProvider(dataSource);
    }

    @Bean
    @Primary
    public EventStorageEngine eventStorageEngine(ConnectionProvider connectionProvider, TransactionManager transactionManager) {
        return new JdbcEventStorageEngine(connectionProvider, transactionManager);
    }

    @Bean
    @Autowired
    public EventStore eventStore(ConnectionProvider connectionProvider) {
        return new EmbeddedEventStore(eventStorageEngine(connectionProvider, NoTransactionManager.INSTANCE));
    }

    @Bean
    public EventTableFactory eventSchemaFactory() {
        return PostgresEventTableFactory.INSTANCE;
    }

    @Bean
    public EventSchema eventSchema() {
        return new EventSchema();
    }

}
