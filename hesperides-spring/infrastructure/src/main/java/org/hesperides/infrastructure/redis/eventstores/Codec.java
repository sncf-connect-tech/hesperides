package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.DomainEventMessage;

import java.util.List;

public interface Codec {

    String code(DomainEventMessage<?> event);

    List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data);
}
