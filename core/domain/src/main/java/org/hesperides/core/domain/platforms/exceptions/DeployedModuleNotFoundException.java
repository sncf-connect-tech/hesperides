package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;

public class DeployedModuleNotFoundException extends NotFoundException {
    public DeployedModuleNotFoundException(Platform.Key platformKey, Module.Key moduleKey, String modulePath) {
        super("Could not find module " + moduleKey.getNamespaceWithoutPrefix() + " with path " + modulePath + " on platform " + platformKey.getApplicationName() + "-" + platformKey.getPlatformName());
    }
    public DeployedModuleNotFoundException(Platform.Key platformKey, String propertiesPath) {
        super("Could not find deployed module with properties path " + propertiesPath + " on platform " + platformKey.getApplicationName() + "-" + platformKey.getPlatformName());
    }
}
