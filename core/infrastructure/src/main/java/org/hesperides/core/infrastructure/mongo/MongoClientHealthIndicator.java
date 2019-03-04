package org.hesperides.core.infrastructure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.hesperides.core.infrastructure.mongo.MongoProjectionRepositoryConfiguration.MONGO_CLIENT_BEAN_NAME;

@Component
public class MongoClientHealthIndicator implements HealthIndicator {

    @Autowired
    @Qualifier(MONGO_CLIENT_BEAN_NAME)
    MongoClient mongoClient;

    @Override
    public Health health() {
        MongoClientOptions mongoClientOptions = mongoClient.getMongoClientOptions();
        Map<String, Object> details = new HashMap<String ,Object>() {{
            put("applicationName", mongoClientOptions.getApplicationName());
            put("description", mongoClientOptions.getDescription());
            put("retryWrites", mongoClientOptions.getRetryWrites());
            put("connectionsPerHost", mongoClientOptions.getConnectionsPerHost());
            put("minConnectionsPerHost", mongoClientOptions.getMinConnectionsPerHost());
            put("threadsAllowedToBlockForConnectionMultiplier", mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier());
            put("maxWaitQueueSize", mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier() * mongoClientOptions.getConnectionsPerHost()); // from MongoClientOptions.java or ConnectionPoolSettings.java
            put("serverSelectionTimeout", mongoClientOptions.getServerSelectionTimeout());
            put("maxWaitTime", mongoClientOptions.getMaxWaitTime());
            put("maxConnectionIdleTime", mongoClientOptions.getMaxConnectionIdleTime());
            put("connectTimeout", mongoClientOptions.getConnectTimeout());
            put("socketTimeout", mongoClientOptions.getSocketTimeout());
            put("heartbeatFrequency", mongoClientOptions.getHeartbeatFrequency());
            put("minHeartbeatFrequency", mongoClientOptions.getMinHeartbeatFrequency());
            put("heartbeatConnectTimeout", mongoClientOptions.getHeartbeatConnectTimeout());
            put("heartbeatSocketTimeout", mongoClientOptions.getHeartbeatSocketTimeout());
            put("localThreshold", mongoClientOptions.getLocalThreshold());
            put("requiredReplicaSetName", mongoClientOptions.getRequiredReplicaSetName());
            put("isSslEnabled", mongoClientOptions.isSslEnabled());
            put("isSslInvalidHostNameAllowed", mongoClientOptions.isSslInvalidHostNameAllowed());
            put("isAlwaysUseMBeans", mongoClientOptions.isAlwaysUseMBeans());
            put("isCursorFinalizerEnabled", mongoClientOptions.isCursorFinalizerEnabled());
            // Sadly spring-boot-actuator does not support nested details: https://github.com/spring-projects/spring-boot/issues/15795
            put("readPreferences.isSlaveOk", mongoClientOptions.getReadPreference().isSlaveOk());
            put("readPreferences.name", mongoClientOptions.getReadPreference().getName());
            put("readConcern.level", mongoClientOptions.getReadConcern().getLevel());
            put("readConcern.isServerDefault", mongoClientOptions.getReadConcern().isServerDefault());
            put("writeConcern.w", mongoClientOptions.getWriteConcern().getWObject());
            put("writeConcern.wTimeoutMS", mongoClientOptions.getWriteConcern().getWtimeout());
            put("writeConcern.fsync", mongoClientOptions.getWriteConcern().getFsync());
            put("writeConcern.journal", mongoClientOptions.getWriteConcern().getJournal());
        }};
        return Health.up().withDetails(details).build();
    }
}
