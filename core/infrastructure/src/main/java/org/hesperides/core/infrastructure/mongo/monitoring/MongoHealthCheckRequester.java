package org.hesperides.core.infrastructure.mongo.monitoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;
import java.time.Instant;

class MongoHealthCheckRequester {

    @Data
    @AllArgsConstructor
    static class MongoHealthCheckResult {
        String version;
        double execTimeInMs;
    }

    static MongoHealthCheckResult performHealthCheck(MongoTemplate mongoTemplate) {
        Instant start = Instant.now();
        Document result = mongoTemplate.executeCommand("{ buildInfo: 1 }");
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        return new MongoHealthCheckResult(
                result.getString("version"),
                duration.getSeconds() * 1000 + duration.getNano() / 1000000.0);
    }
}
