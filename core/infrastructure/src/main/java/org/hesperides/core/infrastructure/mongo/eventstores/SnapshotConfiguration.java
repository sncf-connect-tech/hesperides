/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.infrastructure.mongo.eventstores;

import lombok.Setter;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@ConfigurationProperties("event-store")
public class SnapshotConfiguration {

    @Setter
    private Integer eventsCountToTriggerSnapshot = 1000000;

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
        return new EventCountSnapshotTriggerDefinition(snapshotter, eventsCountToTriggerSnapshot);
    }
}
