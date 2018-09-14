package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class DeployedModuleNotFoundException extends NotFoundException {
    public DeployedModuleNotFoundException(Platform.Key platformKey, String modulePath) {
        super("Could not find module with path " + modulePath + " in platform " + platformKey);
    }
}
