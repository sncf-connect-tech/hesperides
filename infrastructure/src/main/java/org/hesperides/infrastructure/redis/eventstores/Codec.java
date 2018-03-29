package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.TrackedEventData;

import java.util.List;
import java.util.stream.Stream;

public interface Codec {

    String code(DomainEventMessage<?> event);

    List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data);

    Stream<TrackedEventData<?>> decodeAsTrackedDomainEventData(String aggregateIdentifier, long firstSequenceNumber, List<String> data);
}
