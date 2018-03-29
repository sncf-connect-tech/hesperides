package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class RedisLegacyEventStore extends EmbeddedEventStore {
    public RedisLegacyEventStore(LegacyRedisStorageEngine redisStorageEngine) {
        super(redisStorageEngine);
    }
}
