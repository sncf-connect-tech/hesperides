package org.hesperides.infrastructure.eventstores.redis;

import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;

import java.util.List;

public interface Codec {

    String code(DomainEventMessage<?> event);

    List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data);
}
