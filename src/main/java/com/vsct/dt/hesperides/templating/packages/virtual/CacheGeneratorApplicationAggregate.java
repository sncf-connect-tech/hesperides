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
package com.vsct.dt.hesperides.templating.packages.virtual;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.applications.properties.cache.PropertiesCacheLoader;
import com.vsct.dt.hesperides.applications.properties.event.PlatformContainer;
import com.vsct.dt.hesperides.applications.virtual.VirtualPlatformRegistry;
import com.vsct.dt.hesperides.applications.virtual.VirtualSnapshotRegistry;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emeric_martineau on 07/11/2016.
 */
public class CacheGeneratorApplicationAggregate extends AbstractApplicationsAggregate {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheGeneratorApplicationAggregate.class);

    /**
     * Nb event before store cache for force cache system.
     */
    private final long nbEventBeforePersiste;

    /**
     * Count nb event for cache regeneration.
     */
    private long count;

    /**
     * Number of snapshot cache.
     */
    private long snapshotCacheCount;

    /**
     * Platform registry.
     */
    private VirtualPlatformRegistry virtualPlatformRegistry;

    /**
     * Snapshot.
     */
    private VirtualSnapshotRegistry virtualSnapshotRegistry;

    /**
     * Internal structure holding in memory state.
     */
    private PropertiesCacheLoader propertiesCacheLoader;

    /**
     * Constructor.
     *
     * @param store
     * @param hesperidesConfiguration
     */
    public CacheGeneratorApplicationAggregate(final EventStore store,
                                         final HesperidesConfiguration hesperidesConfiguration) {
        super(new EventBus(), store);

        this.virtualPlatformRegistry = new VirtualPlatformRegistry();
        this.virtualSnapshotRegistry = new VirtualSnapshotRegistry();

        this.nbEventBeforePersiste
                = hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste();

        this.propertiesCacheLoader = new PropertiesCacheLoader(store, nbEventBeforePersiste);
    }

    @Override
    protected PlatformRegistryInterface getPlatformRegistry() {
        return this.virtualPlatformRegistry;
    }

    @Override
    protected PropertiesRegistryInterface getPropertiesRegistry() {
        return this.virtualPlatformRegistry;
    }

    @Override
    protected SnapshotRegistryInterface getSnapshotRegistry() {
        return this.virtualSnapshotRegistry;
    }

    public void replay(final String stream) {
        this.count = 0;
        this.snapshotCacheCount = 0;

        this.virtualPlatformRegistry.clear();

        super.replay(stream);
    }

    @Subscribe
    @Override
    public void replayPlatformCreatedEvent(final PlatformCreatedEvent event) {
        super.replayPlatformCreatedEvent(event);

        final PlatformData plateformData = event.getPlatform();

        increaseCounter(new PlatformKey(plateformData.getApplicationName(), plateformData.getPlatformName()));
    }

    @Subscribe
    @Override
    public void replayPlatformUpdatedEvent(final PlatformUpdatedEvent event) {
        super.replayPlatformUpdatedEvent(event);

        final PlatformData plateformData = event.getPlatform();

        increaseCounter(new PlatformKey(plateformData.getApplicationName(), plateformData.getPlatformName()));
    }
    @Subscribe
    @Override
    public void replayPropertiesSavedEvent(final PropertiesSavedEvent event) {
        super.replayPropertiesSavedEvent(event);

        increaseCounter(new PlatformKey(event.getApplicationName(), event.getPlatformName()));
    }

    @Subscribe
    @Override
    public void replayPlateformeDeletedEvent(final PlatformDeletedEvent event) {
        super.replayPlateformeDeletedEvent(event);

        increaseCounter(new PlatformKey(event.getApplicationName(), event.getPlatformName()));
    }

    @Subscribe
    @Override
    public void replaySnapshotTakenEvent(final PlatformSnapshotEvent event) {
        super.replaySnapshotTakenEvent(event);

        increaseCounter(new PlatformKey(event.getApplicationName(), event.getPlatformName()));
    }

    @Subscribe
    @Override
    public void replaySnapshotRestoredEvent(final PlatformSnapshotRestoreEvent event) {
        super.replaySnapshotRestoredEvent(event);

        final PlatformData ptfData = event.getSnapshot().getPlatform();

        increaseCounter(new PlatformKey(ptfData.getApplicationName(), ptfData.getPlatformName()));
    }

    public void clear() {
        this.virtualPlatformRegistry.clear();
    }

    private void increaseCounter(final PlatformKey key) {
        this.count++;

        if (this.count % this.nbEventBeforePersiste == 0) {
            this.snapshotCacheCount++;

            LOGGER.debug("Store new instance (#{}) of cache for {}", this.snapshotCacheCount, key);

            PlatformData ptfData;

            if (this.virtualPlatformRegistry.getPlatform(key).isPresent()) {
                ptfData = this.virtualPlatformRegistry.getPlatform(key).get();
            } else {
                ptfData = null;
            }

            PlatformContainer snapshot = new PlatformContainer(ptfData,
                    this.virtualPlatformRegistry.getProperties(key.getApplicationName(), key.getName()));

            this.propertiesCacheLoader.forceSaveSnapshot(key, snapshot, this.count);
        }
    }
}
