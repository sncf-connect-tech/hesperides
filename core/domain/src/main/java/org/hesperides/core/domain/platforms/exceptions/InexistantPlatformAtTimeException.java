package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class InexistantPlatformAtTimeException extends NotFoundException {
    public InexistantPlatformAtTimeException(Long timestamp) {
        super("Inexistant platform at this time" + (timestamp == null ? "" : " for timestamp " + timestamp));
    }
}
