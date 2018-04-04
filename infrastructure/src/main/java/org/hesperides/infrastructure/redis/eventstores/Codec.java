package org.hesperides.infrastructure.redis.eventstores;

import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.GlobalSequenceTrackingToken;
import org.axonframework.eventsourcing.eventstore.TrackedEventData;

import java.util.List;
import java.util.stream.Stream;

public interface Codec {

    String code(DomainEventMessage<?> event);

    List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data);

    TrackedEventData<?> decodeEventAsTrackedDomainEventData(String aggregateIdentifier, long firstSequenceNumber, String data, GlobalSequenceTrackingToken trackingToken);
}
