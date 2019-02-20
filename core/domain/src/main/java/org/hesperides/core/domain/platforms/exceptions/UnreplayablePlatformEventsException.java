package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class UnreplayablePlatformEventsException extends NotFoundException {
    public UnreplayablePlatformEventsException(Long timestamp) {
        super("Could not replay platform events for timestamp " + timestamp);
    }
}
