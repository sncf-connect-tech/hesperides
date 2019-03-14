package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;

public class InstanceNotFoundException extends NotFoundException {
    public InstanceNotFoundException(Platform.Key platformKey, Module.Key moduleKey, String modulePath, String instanceName) {
        super("Could not find instance " + instanceName + " for module " + moduleKey.getNamespaceWithoutPrefix() + " with path " + modulePath + " in platform " + platformKey.getApplicationName() + "-" + platformKey.getPlatformName());
    }
}
