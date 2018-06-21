package org.hesperides.domain.platforms.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

public class PlatformNotFoundException extends NotFoundException {
    public PlatformNotFoundException(Platform.Key platformKey) {
        super("Could not find platform info for " + platformKey);
    }
}
