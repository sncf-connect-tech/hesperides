package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.entities.Platform;

@Value
public class PlatformCreatedEventIO {
    private Platform platformCreated;
    private String user;

    public PlatformCreatedEventIO(PlatformCreatedEvent platformCreatedEvent) {
        this.platformCreated = platformCreatedEvent.getPlatform();
        this.user = platformCreatedEvent.getUser();

    }
}
