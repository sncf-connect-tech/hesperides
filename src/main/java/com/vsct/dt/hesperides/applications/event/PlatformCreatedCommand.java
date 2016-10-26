package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformCreatedEvent;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformCreatedCommand extends AbstractPlatformEvent<PlatformCreatedEvent> {
    private final PlatformData platform;
    private final PlatformRegistryInterface platformRegistry;

    /**
     * The new platform to store in cahe.
     */
    private PlatformData newPlatform;

    public PlatformCreatedCommand(final PlatformRegistryInterface platformRegistry, final PlatformData platform) {
        this.platform = platform;
        this.platformRegistry = platformRegistry;
    }

    @Override
    public PlatformCreatedEvent apply() {
        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();

        String applicationVersion = platform.getApplicationVersion();
        boolean isProductionPlatform = platform.isProduction();

        Optional<PlatformData> existingPlatform = this.platformRegistry.getPlatform(platform.getKey());

        if (existingPlatform.isPresent()) {
            throw new DuplicateResourceException("PlatformData " + platform + " already exists");
        }

        Set<ApplicationModuleData> modulesWithIds = generateSetOfModulesWithIds(platform.getModules());

        this.newPlatform = PlatformData.withPlatformName(platformName)
                .withApplicationName(applicationName)
                .withApplicationVersion(applicationVersion)
                .withModules(modulesWithIds)
                .withVersion(1L)
                .setProduction(isProductionPlatform)
                .build();

        return new PlatformCreatedEvent(applicationName, newPlatform);
    }

    @Override
    public void complete() {
        this.platformRegistry.createOrUpdatePlatform(newPlatform);
    }
}
