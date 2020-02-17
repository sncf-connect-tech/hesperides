package org.hesperides.core.domain.events.queries;

import lombok.Value;
import org.hesperides.core.domain.security.UserEvent;

@Value
public class EventView {
    String type;
    UserEvent data;
    String timestamp;
}
