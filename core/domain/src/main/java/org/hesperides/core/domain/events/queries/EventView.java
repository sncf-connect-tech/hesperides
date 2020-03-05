package org.hesperides.core.domain.events.queries;

import lombok.Value;
import org.hesperides.core.domain.security.UserEvent;

import java.time.Instant;

@Value
public class EventView {
    String type;
    UserEvent data;
    Instant timestamp;
}
