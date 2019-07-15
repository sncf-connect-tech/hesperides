/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hesperides.core.infrastructure.mongo.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import static org.hesperides.core.infrastructure.mongo.monitoring.CustomMongoHealthIndicator.getMongoClient;
import static org.hesperides.core.infrastructure.mongo.monitoring.CustomMongoHealthIndicator.getServerSessionPool;
import static org.hesperides.core.infrastructure.mongo.monitoring.MongoHealthCheckRequester.performHealthCheck;

// Permet d'exposer une métrique Prometheus mesurant la latence MongoDB
public class MongoHealthProbe implements MeterBinder {

    private String repoName;
    private MongoTemplate mongoTemplate;

    MongoHealthProbe(String repoName, MongoTemplate mongoTemplate) {
        this.repoName = repoName;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void bindTo(final MeterRegistry meterRegistry) {
        Gauge.builder(repoName + "_mongoHealthCheckQueryTime", this,
                probe -> performHealthCheck(probe.mongoTemplate).getExecTimeInMs())
                .baseUnit("ms")
                .register(meterRegistry);
        Gauge.builder(repoName + "_mongoSessionPoolInUseCount", this,
                probe -> getServerSessionPool(getMongoClient((SimpleMongoDbFactory) probe.mongoTemplate.getMongoDbFactory())).getInUseCount())
                .register(meterRegistry);
    }
}
