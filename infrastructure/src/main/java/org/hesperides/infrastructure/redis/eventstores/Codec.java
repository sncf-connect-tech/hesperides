package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;

import java.util.Collections;
import java.util.List;

public interface Codec {

    String code(DomainEventMessage<?> event);

    List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data);

    default DomainEventMessage<?> decode(String aggregateIdentifier, long firstSequenceNumber, String data) {
        return decode(aggregateIdentifier, firstSequenceNumber, Collections.singletonList(data)).get(0);
    }
}
