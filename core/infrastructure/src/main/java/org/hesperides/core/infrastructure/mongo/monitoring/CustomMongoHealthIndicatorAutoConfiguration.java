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

import org.hesperides.core.infrastructure.mongo.MongoProjectionRepositoryConfiguration;
import org.hesperides.core.infrastructure.mongo.eventstores.AxonMongoEventStoreConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

// Inspiré de : https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/mongo/MongoHealthIndicatorAutoConfiguration.java
@Configuration
public class CustomMongoHealthIndicatorAutoConfiguration extends
        CompositeHealthIndicatorConfiguration<CustomMongoHealthIndicator, MongoTemplate> {

    @Autowired
    private Map<String, MongoTemplate> mongoTemplates;

    @Bean(name = "mongoHealthIndicator")
    public HealthIndicator mongoHealthIndicator() {
        return createHealthIndicator(this.mongoTemplates);
    }

    @Bean
    @ConditionalOnBean(name = AxonMongoEventStoreConfiguration.MONGO_TEMPLATE_BEAN_NAME)
    public static MongoHealthProbe axonEventStoreRepositoryLatencyProbe(@Qualifier(AxonMongoEventStoreConfiguration.MONGO_TEMPLATE_BEAN_NAME) MongoTemplate mongoTemplate) {
        return new MongoHealthProbe("axonEventStore", mongoTemplate);
    }

    @Bean
    public static MongoHealthProbe mongoProjectionRepositoryLatencyProbe(@Qualifier(MongoProjectionRepositoryConfiguration.MONGO_TEMPLATE_BEAN_NAME) MongoTemplate mongoTemplate) {
        return new MongoHealthProbe("mongoProjection", mongoTemplate);
    }

}
