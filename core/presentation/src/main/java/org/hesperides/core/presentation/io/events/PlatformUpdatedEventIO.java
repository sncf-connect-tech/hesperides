package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.platforms.entities.Platform;

@Value
public class PlatformUpdatedEventIO {
    private Platform platformUpdated;

    public PlatformUpdatedEventIO(PlatformUpdatedEvent platformUpdatedEvent) {
        this.platformUpdated = platformUpdatedEvent.getPlatform();
    }
}
