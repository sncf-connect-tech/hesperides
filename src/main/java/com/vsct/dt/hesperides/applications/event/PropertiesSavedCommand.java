package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.PropertiesSavedEvent;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PropertiesSavedCommand implements HesperidesCommand<PropertiesSavedEvent> {
    private final PlatformRegistryInterface platformRegistry;
    private final PropertiesRegistryInterface propertiesRegistry;
    private final PlatformKey platformKey;
    private final String path;
    private final PropertiesData properties;
    private final long platformVersionId;
    private final String comment;

    /**
     * The new platform to store in cache.
     */
    private PlatformData updatedPlatform;

    public PropertiesSavedCommand(final PlatformRegistryInterface platformRegistry,
                                  final PropertiesRegistryInterface propertiesRegistry, final PlatformKey platformKey,
                                  final String path, final PropertiesData properties, final long platformVersionId,
                                  final String comment) {
        this.platformRegistry = platformRegistry;
        this.propertiesRegistry = propertiesRegistry;
        this.platformKey = platformKey;
        this.path = path;
        this.properties = properties;
        this.platformVersionId = platformVersionId;
        this.comment = comment;
    }

    @Override
    public PropertiesSavedEvent apply() {
        final String applicationName = platformKey.getApplicationName();
        final String platformName = platformKey.getName();
        final long platformVID = platformVersionId;

        final Optional<PlatformData> optionalPlatform = platformRegistry.getPlatform(platformKey);

        if (optionalPlatform.isPresent()) {
            final PlatformData platform = optionalPlatform.get();
            platform.tryCompareVersionID(platformVID);
            //Properties are immutable, it is ok to store them directly

            updatedPlatform = PlatformData.withPlatformName(platform.getPlatformName())
                    .withApplicationName(platform.getApplicationName())
                    .withApplicationVersion(platform.getApplicationVersion())
                    .withModules(platform.getModules())
                    .withVersion(platform.getVersionID() + 1)
                    .setProduction(platform.isProduction())
                    .build();

            return new PropertiesSavedEvent(applicationName, platformName, path, properties, comment);
        } else {
            throw new MissingResourceException("Cannot create properties because platform " + applicationName + "/" + platformName + " does not exist");
        }
    }

    @Override
    public void complete() {
        final String applicationName = platformKey.getApplicationName();
        final String platformName = platformKey.getName();

        propertiesRegistry.createOrUpdateProperties(applicationName, platformName, path, properties);

        platformRegistry.createOrUpdatePlatform(updatedPlatform);
    }
}
