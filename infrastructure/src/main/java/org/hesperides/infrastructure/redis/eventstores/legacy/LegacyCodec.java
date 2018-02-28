package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.Gson;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.infrastructure.redis.eventstores.Codec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Attention, ce qui va suivre est très moche...
 * Mais ça fait le boulot, c'est à peu près lisible et c'est censé disparaître (lol)
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
        } else {
            throw new UnsupportedOperationException("Serialization for class " + event.getPayloadType() + " is not implemented");
        }

        return new Gson().toJson(new LegacyEvent(eventType, data, getLegacyTimestampFromEventTimestamp(event.getTimestamp()), getContextUsername()));
    }

    /**
     * J'aurais préféré mettre ces deux méthodes dans LegacyEvent mais ça simplifie les tests
     */
    protected Long getLegacyTimestampFromEventTimestamp(Instant timestamp) {
        return Timestamp.from(timestamp).getTime();
    }

    protected String getContextUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data) {
        List<DomainEventMessage<?>> events = new ArrayList<>();

        for (String legacyJsonData : data) {
            LegacyEvent legacyEvent = new Gson().fromJson(legacyJsonData, LegacyEvent.class);
            switch (legacyEvent.getEventType()) {
                case LegacyModuleCreatedEvent.EVENT_TYPE:
                    events.add(LegacyModuleCreatedEvent.toDomainEventMessage(legacyEvent.getData(), aggregateIdentifier, firstSequenceNumber));
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
