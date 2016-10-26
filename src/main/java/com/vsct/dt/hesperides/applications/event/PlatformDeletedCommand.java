package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformDeletedEvent;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformDeletedCommand implements HesperidesCommand<PlatformDeletedEvent> {
    private final PlatformKey key;
    private final PlatformRegistryInterface platformRegistry;
    private final PropertiesRegistryInterface propertiesRegistry;

    public PlatformDeletedCommand(final PlatformRegistryInterface platformRegistry,
                                  final PropertiesRegistryInterface propertiesRegistry, final PlatformKey key) {
        this.key = key;
        this.propertiesRegistry = propertiesRegistry;
        this.platformRegistry = platformRegistry;
    }

    @Override
    public PlatformDeletedEvent apply() {
        final String platformName = key.getName();
        final String applicationName = key.getApplicationName();

        final Optional<PlatformData> platformOptional = platformRegistry.getPlatform(key);

        if (platformOptional.isPresent()) {
            return new PlatformDeletedEvent(applicationName, platformName);
        } else {
            throw new MissingResourceException(key + " does not exists");
        }
    }

    @Override
    public void complete() {
        //remove platform
        platformRegistry.deletePlatform(key);
    }
}
