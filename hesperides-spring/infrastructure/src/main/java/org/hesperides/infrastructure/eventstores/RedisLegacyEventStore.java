package org.hesperides.infrastructure.eventstores;

import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.*;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.hesperides.infrastructure.eventstores.redis.RedisStorageEngine;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Profile("!local")
public class RedisLegacyEventStore extends EmbeddedEventStore {
    public RedisLegacyEventStore(RedisStorageEngine redisStorageEngine){
        super(redisStorageEngine);
    }
}
