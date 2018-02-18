package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.DomainEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "redis", name = "codec", havingValue = "legacy")
class LegacyCodec implements Codec {

    @Override
    public String code(DomainEventMessage event) {
        throw new IllegalArgumentException("todo");
    }

    @Override
    public List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data) {
        throw new IllegalArgumentException("todo");
    }
}
