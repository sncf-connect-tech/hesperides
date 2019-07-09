package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;

public class UnreplayablePlatformEventsException extends NotFoundException {
    public UnreplayablePlatformEventsException(Long timestamp, Throwable throwable) {
        super("Could not replay platform events" + (timestamp == null ? "" : " for timestamp " + timestamp) + ": " + throwable.getMessage());
    }
}
