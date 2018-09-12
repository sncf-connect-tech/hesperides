package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.security.UserEvent;

import java.time.Instant;

@Value
public class EventOutput {

    String type;
    UserEvent data;
    Instant timestamp;
    String userName;

    public EventOutput(EventView view) {
        this.type = view.getType();
        this.data = view.getData();
        this.timestamp = view.getTimestamp();
        this.userName = view.getData().getUser().getName();
    }
}
