package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.GenericDomainEventEntry;
import org.axonframework.eventsourcing.eventstore.GlobalSequenceTrackingToken;
import org.axonframework.eventsourcing.eventstore.TrackedDomainEventData;
import org.axonframework.eventsourcing.eventstore.TrackedEventData;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.security.UserEvent;
import org.hesperides.infrastructure.redis.eventstores.Codec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * C'est moche mais ça fait le boulot, c'est lisible et c'est censé disparaître.
 * On a tenté de gérer ce mapping avec la librairie Jolt, mais la transformation est trop complexe.
 * Donc on le fait à la main.
 * J'assume ce choix (Thomas L'Hostis)
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "redis", name = "codec", havingValue = "legacy", matchIfMissing = true)
class LegacyCodec implements Codec {

    @Override
    public String code(DomainEventMessage event) {
        String eventType, data;

        if (event.getPayload() instanceof ModuleCreatedEvent) {
            eventType = LegacyModuleCreatedEvent.EVENT_TYPE;
            data = LegacyModuleCreatedEvent.fromDomainEventMessage(event);
        } else if (event.getPayload() instanceof ModuleUpdatedEvent) {
            eventType = LegacyModuleUpdatedEvent.EVENT_TYPE;
            data = LegacyModuleUpdatedEvent.fromDomainEventMessage(event);
        } else if (event.getPayload() instanceof ModuleDeletedEvent) {
            eventType = LegacyModuleDeletedEvent.EVENT_TYPE;
            data = LegacyModuleDeletedEvent.fromDomainEventMessage(event);
        } else if (event.getPayload() instanceof TemplateCreatedEvent) {
            eventType = LegacyTemplateCreatedEvent.EVENT_TYPE;
            data = LegacyTemplateCreatedEvent.fromDomainEventMessage(event);
        } else if (event.getPayload() instanceof TemplateUpdatedEvent) {
            eventType = LegacyTemplateUpdatedEvent.EVENT_TYPE;
            data = LegacyTemplateUpdatedEvent.fromDomainEventMessage(event);
        } else if (event.getPayload() instanceof TemplateDeletedEvent) {
            eventType = LegacyTemplateDeletedEvent.EVENT_TYPE;
            data = LegacyTemplateDeletedEvent.fromDomainEventMessage(event);
        } else {
            throw new UnsupportedOperationException("Serialization for class " + event.getPayloadType() + " is not implemented");
        }

        String username = ((UserEvent) event.getPayload()).getUser().getName();
        Long timestamp = getLegacyTimestampFromEventTimestamp(event.getTimestamp());
        return new Gson().toJson(new LegacyEvent(eventType, data, timestamp, username));
    }

    /**
     * J'aurais préféré mettre cette méthode dans LegacyEvent mais ça simplifie les tests
     */
    protected Long getLegacyTimestampFromEventTimestamp(Instant timestamp) {
        return Timestamp.from(timestamp).getTime();
    }

    @Override
    public TrackedEventData<?> decodeEventAsTrackedDomainEventData(String aggregateIdentifier, long firstSequenceNumber, String data, GlobalSequenceTrackingToken trackingToken) {

        DomainEventMessage<?> domainEventMessage = decode(aggregateIdentifier, firstSequenceNumber, Collections.singletonList(data)).get(0);

        return new TrackedDomainEventData<>(trackingToken,  new GenericDomainEventEntry<>(domainEventMessage.getType(),
                domainEventMessage.getAggregateIdentifier(),
                domainEventMessage.getSequenceNumber(),
                "",
                domainEventMessage.getTimestamp(),
                domainEventMessage.getPayloadType().getName(),
                null,
                domainEventMessage.getPayload(),
                "")
        );
    }

    @Override
    public List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data) {
        List<DomainEventMessage<?>> events = new ArrayList<>();

        for (String legacyJsonData : data) {
            LegacyEvent legacyEvent = new Gson().fromJson(legacyJsonData, LegacyEvent.class);
            switch (legacyEvent.getEventType()) {
                case LegacyModuleCreatedEvent.EVENT_TYPE:
                    events.add(LegacyModuleCreatedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                case LegacyModuleUpdatedEvent.EVENT_TYPE:
                    events.add(LegacyModuleUpdatedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                case LegacyModuleDeletedEvent.EVENT_TYPE:
                    events.add(LegacyModuleDeletedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                case LegacyTemplateCreatedEvent.EVENT_TYPE:
                    events.add(LegacyTemplateCreatedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                case LegacyTemplateUpdatedEvent.EVENT_TYPE:
                    events.add(LegacyTemplateUpdatedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                case LegacyTemplateDeletedEvent.EVENT_TYPE:
                    events.add(LegacyTemplateDeletedEvent.toDomainEventMessage(legacyEvent, aggregateIdentifier, firstSequenceNumber));
                    break;
                default:
                    throw new UnsupportedOperationException("Deserialization for class " + legacyEvent.getEventType() + " is not implemented");
            }
        }

        return events;
    }
}
