package org.hesperides.core.domain.events.queries;

import lombok.Value;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.core.domain.security.UserEvent;

import java.time.Instant;

@Value
public class EventView {

    String type;
    UserEvent data;
    Instant timestamp;

    public EventView(final DomainEventMessage domainEventMessage) {
        this.type = domainEventMessage.getPayload().getClass().getName();
        this.data = ((UserEvent) domainEventMessage.getPayload());
        this.timestamp = domainEventMessage.getTimestamp();
    }
}
