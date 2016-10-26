package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.PlatformSnapshot;
import com.vsct.dt.hesperides.applications.PlatformSnapshotRestoreEvent;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformSnapshotRestoreCommand implements HesperidesCommand<PlatformSnapshotRestoreEvent> {
    private final PlatformRegistryInterface platformRegistry;
    private final PropertiesRegistryInterface propertiesRegistry;
    private final long timestamp;
    private final PlatformSnapshot snapshot;

    private PlatformKey key;
    private PlatformData update;

    public PlatformSnapshotRestoreCommand(final PlatformRegistryInterface platformRegistry,
                                          final PropertiesRegistryInterface propertiesRegistry,
                                          final long timestamp, final PlatformSnapshot snapshot) {
        this.platformRegistry = platformRegistry;
        this.propertiesRegistry = propertiesRegistry;
        this.timestamp = timestamp;
        this.snapshot = snapshot;
    }

    @Override
    public PlatformSnapshotRestoreEvent apply() {
        final PlatformData snapshotedPlatform = snapshot.getPlatform();

        key = snapshot.getPlatform().getKey();

        //We need to be cautious with version id to avoid messing up everything
        //First get the platformVid
        final Optional<PlatformData> optionalExistingPlatform = platformRegistry.getPlatform(key);
        long vid = 0;

        if (optionalExistingPlatform.isPresent()) {
            //PlatformData exist we get the vid
            PlatformData existingPlatform = optionalExistingPlatform.get();
            vid = existingPlatform.getVersionID();
        }

        update = PlatformData.withPlatformName(snapshotedPlatform.getPlatformName())
                .withApplicationName(snapshotedPlatform.getApplicationName())
                .withApplicationVersion(snapshotedPlatform.getApplicationVersion())
                .withModules(snapshotedPlatform.getModules())
                .withVersion(vid + 1)
                .setProduction(snapshotedPlatform.isProduction())
                .build();

        return new PlatformSnapshotRestoreEvent(timestamp, snapshot);
    }

    @Override
    public void complete() {
        platformRegistry.createOrUpdatePlatform(update);

        //Add all the properties
        snapshot.getProperties().forEach((path, properties) ->
                propertiesRegistry.createOrUpdateProperties(key.getApplicationName(), key.getName(), path, properties)
        );
    }
}
