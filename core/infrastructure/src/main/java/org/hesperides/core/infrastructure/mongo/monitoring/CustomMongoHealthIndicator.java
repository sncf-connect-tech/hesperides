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

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.connection.Cluster;
import com.mongodb.internal.session.ServerSessionPool;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoDbFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hesperides.core.infrastructure.mongo.monitoring.MongoHealthCheckRequester.performHealthCheck;

// Ce HealthIndicator nous permet de monitorer le client Java Mongo,
// notamment en termes de latence de connexion et de taux d'usage du pool.
// Inspiré de : https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/mongo/MongoHealthIndicator.java
// Remplace le /manage/health/mongo généré par défaut
public class CustomMongoHealthIndicator extends AbstractHealthIndicator {

    private final MongoTemplate mongoTemplate;

    public CustomMongoHealthIndicator(MongoTemplate mongoTemplate) {
        super("MongoDB health check failed");
        Assert.notNull(mongoTemplate, "MongoTemplate must not be null");
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        // Les "hacks" d'appels à des méthodes privées sont nécessaires pour obtenir le serverSessionPoolInUseCount bien utile
        MongoClient mongoClient = getMongoClient((SimpleMongoDbFactory) this.mongoTemplate.getMongoDbFactory());
        builder.up()
                .withDetail("healthcheck", performHealthCheck(mongoTemplate))
                .withDetail("config", getMongoClientOptionsAsMap(mongoClient.getMongoClientOptions()))
                .withDetail("serverSessionPoolInUseCount", getServerSessionPool(mongoClient).getInUseCount())
                .withDetail("clusterTime", Optional.ofNullable(getCluster(mongoClient).getClusterTime()).map(BsonTimestamp::getValue).orElse(0L));
    }

    private static Map<String, Object> getMongoClientOptionsAsMap(MongoClientOptions mongoClientOptions) {
        Map<String, Object> config = new HashMap<>();
        config.put("applicationName", mongoClientOptions.getApplicationName());
        config.put("description", mongoClientOptions.getDescription());
        config.put("retryWrites", mongoClientOptions.getRetryWrites());
        config.put("connectionsPerHost", mongoClientOptions.getConnectionsPerHost());
        config.put("minConnectionsPerHost", mongoClientOptions.getMinConnectionsPerHost());
        config.put("threadsAllowedToBlockForConnectionMultiplier", mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier());
        config.put("maxWaitQueueSize", mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier() * mongoClientOptions.getConnectionsPerHost()); // from MongoClientOptions.java or ConnectionPoolSettings.java
        config.put("serverSelectionTimeout", mongoClientOptions.getServerSelectionTimeout());
        config.put("maxWaitTime", mongoClientOptions.getMaxWaitTime());
        config.put("maxConnectionIdleTime", mongoClientOptions.getMaxConnectionIdleTime());
        config.put("connectTimeout", mongoClientOptions.getConnectTimeout());
        config.put("socketTimeout", mongoClientOptions.getSocketTimeout());
        config.put("heartbeatFrequency", mongoClientOptions.getHeartbeatFrequency());
        config.put("minHeartbeatFrequency", mongoClientOptions.getMinHeartbeatFrequency());
        config.put("heartbeatConnectTimeout", mongoClientOptions.getHeartbeatConnectTimeout());
        config.put("heartbeatSocketTimeout", mongoClientOptions.getHeartbeatSocketTimeout());
        config.put("localThreshold", mongoClientOptions.getLocalThreshold());
        config.put("requiredReplicaSetName", mongoClientOptions.getRequiredReplicaSetName());
        config.put("isSslEnabled", mongoClientOptions.isSslEnabled());
        config.put("isSslInvalidHostNameAllowed", mongoClientOptions.isSslInvalidHostNameAllowed());
        config.put("isAlwaysUseMBeans", mongoClientOptions.isAlwaysUseMBeans());
        config.put("isCursorFinalizerEnabled", mongoClientOptions.isCursorFinalizerEnabled());
        // Sadly spring-boot-actuator does not support nested details: https://github.com/spring-projects/spring-boot/issues/15795
        config.put("readPreferences.isSlaveOk", mongoClientOptions.getReadPreference().isSlaveOk());
        config.put("readPreferences.name", mongoClientOptions.getReadPreference().getName());
        config.put("readConcern.level", mongoClientOptions.getReadConcern().getLevel());
        config.put("readConcern.isServerDefault", mongoClientOptions.getReadConcern().isServerDefault());
        config.put("writeConcern.w", mongoClientOptions.getWriteConcern().getWObject());
        config.put("writeConcern.wTimeoutMS", mongoClientOptions.getWriteConcern().getWtimeout());
        config.put("writeConcern.fsync", mongoClientOptions.getWriteConcern().getFsync());
        config.put("writeConcern.journal", mongoClientOptions.getWriteConcern().getJournal());
        return config;
    }

    /* Private methods accessors: */

    static MongoClient getMongoClient(SimpleMongoDbFactory mongoClient) {
        try {
            Method privateMethod = MongoDbFactorySupport.class.getDeclaredMethod("getMongoClient", null);
            privateMethod.setAccessible(true);
            return (MongoClient) privateMethod.invoke(mongoClient);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static ServerSessionPool getServerSessionPool(MongoClient mongoClient) {
        try {
            Method privateMethod = Mongo.class.getDeclaredMethod("getServerSessionPool", null);
            privateMethod.setAccessible(true);
            return (ServerSessionPool) privateMethod.invoke(mongoClient);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static Cluster getCluster(MongoClient mongoClient) {
        try {
            Method privateMethod = Mongo.class.getDeclaredMethod("getCluster", null);
            privateMethod.setAccessible(true);
            return (Cluster) privateMethod.invoke(mongoClient);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
