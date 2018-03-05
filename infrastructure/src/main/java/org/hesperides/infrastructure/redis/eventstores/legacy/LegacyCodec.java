package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.Gson;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.security.UserEvent;
import org.hesperides.infrastructure.redis.eventstores.Codec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * C'est moche mais ça fait le boulot, c'est lisible et c'est censé disparaître.
 * On a tenté de gérer ce mapping avec la librairie Jolt, mais la transformation est trop complexe.
 * Donc on le fait à la main.
 * J'assume ce choix (Thomas L'Hostis)
 */
@Component
@ConditionalOnProperty(prefix = "redis", name = "codec", havingValue = "legacy", matchIfMissing = true)
class LegacyCodec implements Codec {

    @Override
    public String code(DomainEventMessage event) {
        String eventType, data;

        if (event.getPayload() instanceof ModuleCreatedEvent) {
            eventType = LegacyModuleCreatedEvent.EVENT_TYPE;
            data = LegacyModuleCreatedEvent.fromDomainEvent((ModuleCreatedEvent) event.getPayload());
//        } else if (event.getPayload() instanceof AnotherEvent) {
//            eventType = AnotherLegacyEvent.EVENT_TYPE;
//            data = AnotherLegacyEvent.fromDomainEvent((AnotherEvent) event.getPayload());
        } else if (event.getPayload() instanceof ModuleUpdatedEvent) {
            eventType = LegacyModuleUpdatedEvent.EVENT_TYPE;
            data = LegacyModuleUpdatedEvent.fromDomainEvent((ModuleUpdatedEvent) event.getPayload());
        } else {
            throw new UnsupportedOperationException("Serialization for class " + event.getPayloadType() + " is not implemented");
        }

        String user = ((UserEvent) event.getPayload()).getUser().getName();
        return new Gson().toJson(new LegacyEvent(eventType, data, getLegacyTimestampFromEventTimestamp(event.getTimestamp()), user));
    }

    /**
     * J'aurais préféré mettre ces deux méthodes dans LegacyEvent mais ça simplifie les tests
     */
    protected Long getLegacyTimestampFromEventTimestamp(Instant timestamp) {
        return Timestamp.from(timestamp).getTime();
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
//                case AnotherLegacyEvent.EVENT_TYPE:
//                    events.add(AnotherLegacyEvent.toDomainEventMessage(legacyEvent.getData(), aggregateIdentifier, firstSequenceNumber));
//                    break;
                default:
                    throw new UnsupportedOperationException("Deserialization for class " + legacyEvent.getEventType() + " is not implemented");
            }
        }

        return events;
    }
}
