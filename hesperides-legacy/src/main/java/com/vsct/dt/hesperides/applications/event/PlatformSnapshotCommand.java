/*
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformSnapshotCommand implements HesperidesCommand<PlatformSnapshotEvent> {
    private final PlatformRegistryInterface platformRegistry;
    private final PropertiesRegistryInterface propertiesRegistry;
    private final SnapshotRegistryInterface snapshotRegistryInterface;
    private final PlatformKey key;
    private final long timestamp;

    private PlatformSnapshot platformSnapshot;
    private PlatformSnapshotKey snapshotKey;

    public PlatformSnapshotCommand(final PlatformRegistryInterface platformRegistry,
                                   final PropertiesRegistryInterface propertiesRegistry,
                                   final SnapshotRegistryInterface snapshotRegistryInterface,
                                   final PlatformKey key, final long timestamp) {
        this.platformRegistry = platformRegistry;
        this.propertiesRegistry = propertiesRegistry;
        this.snapshotRegistryInterface = snapshotRegistryInterface;
        this.key = key;
        this.timestamp = timestamp;
    }

    @Override
    public PlatformSnapshotEvent apply() {
        final Optional<PlatformData> platformOptional = platformRegistry.getPlatform(key);

        if (platformOptional.isPresent()) {

            //Create the platform object
            //We only save the properties related to modules defined in the platform. Properties from older modules (still in properties registry) are not kept
            final PlatformData platform = platformOptional.get();
            final Map<String, PropertiesData> allPlatformProperties = new HashMap<>();

            platform.getModules().forEach(module -> {
                String path = module.getPropertiesPath();
                Optional<PropertiesData> optionalProperties = this.propertiesRegistry.getProperties(
                        key.getApplicationName(), key.getName(), path);

                if (optionalProperties.isPresent()) {
                    allPlatformProperties.put(path, optionalProperties.get());
                }
            });

            platformSnapshot = new PlatformSnapshot(platform, allPlatformProperties);

            //Put the snapshot in the snapshot registry
            snapshotKey = new PlatformSnapshotKey(timestamp, key);

            //Fire an event
            return new PlatformSnapshotEvent(timestamp, key.getApplicationName(), key.getName());

        } else {
            throw new MissingResourceException(key + " does not exists");
        }
    }

    @Override
    public void complete() {
        snapshotRegistryInterface.createSnapshot(snapshotKey, platformSnapshot);
    }
}
