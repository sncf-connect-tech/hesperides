package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class PlatformNotFoundException extends NotFoundException {
    public PlatformNotFoundException(Platform.Key platformKey) {
        super("Could not find platform info for " + platformKey.getApplicationName() + "-" + platformKey.getPlatformName());
    }

    public PlatformNotFoundException(String platformId) {
        super("Could not find platform info for " + platformId);
    }
}
