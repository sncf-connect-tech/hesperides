package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class PlatformNotFoundException extends NotFoundException {
    public PlatformNotFoundException(Platform.Key platformKey) {
        super("Could not find platform info for " + platformKey);
    }
}
