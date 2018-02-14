package org.hesperides.infrastructure.eventstores;

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.infrastructure.eventstores.redis.RedisStorageEngine;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class RedisLegacyEventStore extends EmbeddedEventStore {
    public RedisLegacyEventStore(RedisStorageEngine redisStorageEngine) {
        super(redisStorageEngine);
    }
}
