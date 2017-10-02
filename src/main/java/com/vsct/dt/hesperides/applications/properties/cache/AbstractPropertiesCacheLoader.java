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

package com.vsct.dt.hesperides.applications.properties.cache;

import com.google.common.cache.CacheLoader;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.cache.ApplicationStoragePrefixInterface;
import com.vsct.dt.hesperides.applications.properties.event.PlatformContainer;
import com.vsct.dt.hesperides.applications.virtual.VirtualApplicationsAggregate;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.HesperidesSnapshotItem;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import org.slf4j.Logger;

import java.util.*;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public abstract class AbstractPropertiesCacheLoader<K> extends CacheLoader<K, PlatformContainer>
        implements ApplicationStoragePrefixInterface {
    /**
     * Event store.
     */
    private final EventStore store;

    /**
     * Nb events before store cache.
     */
    private final long nbEventBeforePersiste;

    public AbstractPropertiesCacheLoader(final EventStore store, final long nbEventBeforePersiste) {
        this.store = store;
        this.nbEventBeforePersiste = nbEventBeforePersiste;
    }

    /**
     * Return logger.
     *
     * @return
     */
    protected abstract Logger getLogger();

    /**
     * Load properties from database.
     *
     * @param key key
     * @param timestamp timestamp
     *
     * @return properties
     *
     * @throws Exception if not found.
     */
    protected PlatformContainer loadProperties(final PlatformKey key, final long timestamp) throws Exception {
        if (timestamp == Long.MAX_VALUE) {
            getLogger().debug("Load properties with key '{}'.", key);
        } else {
            getLogger().debug("Load properties with key '{}' on timestamp '{}'.", key, timestamp);
        }
        // Redis key pattern to search all application platform
        final String redisKey = generateDbKey(key);

        // Properties builder
        PlatformContainer propertiesBuilder;
        Optional<HesperidesSnapshotItem> hesperidesSnapshotItem;

        if (timestamp == Long.MAX_VALUE) {
            // In case of max value, we get last snapshot
            hesperidesSnapshotItem = store.findLastSnapshot(redisKey);
        } else {
            hesperidesSnapshotItem = store.findSnapshot(redisKey, nbEventBeforePersiste, timestamp);
        }

        if (hesperidesSnapshotItem.isPresent()) {
            final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

            propertiesBuilder = (PlatformContainer) snapshot.getSnapshot();

            // If snapshot is done on last event, do nothing
            if (snapshot.getStreamNbEvents() <= snapshot.getCacheNbEvents()) {
                return propertiesBuilder;
            }

            final VirtualApplicationsAggregate virtualApplicationsAggregate = new VirtualApplicationsAggregate(
                    store,
                    propertiesBuilder.getPlatform(),
                    propertiesBuilder.getProperties());
            final long start = snapshot.getCacheNbEvents();
            final long stop = snapshot.getStreamNbEvents();

            // If no timestamp, but snapshot missing last event, replay it until at end
            if (timestamp == Long.MAX_VALUE) {
                virtualApplicationsAggregate.replay(redisKey, start, stop);
            } else if (snapshot.getStreamNbEvents() > snapshot.getCacheNbEvents()) {
                virtualApplicationsAggregate.replay(redisKey, start, stop, timestamp);
            }

            updatePropertiesContainer(propertiesBuilder, virtualApplicationsAggregate);
        } else {
            propertiesBuilder = new PlatformContainer();

            final VirtualApplicationsAggregate virtualApplicationsAggregate = new VirtualApplicationsAggregate(store);

            if (timestamp == Long.MAX_VALUE) {
                virtualApplicationsAggregate.replay(redisKey);
            } else {
                virtualApplicationsAggregate.replay(redisKey, 0, Long.MAX_VALUE, timestamp);
            }

            updatePropertiesContainer(propertiesBuilder, virtualApplicationsAggregate);
        }

        // Can't return null !!!!
        return propertiesBuilder;
    }

    /**
     * Update properties container.
     *
     * @param propertiesBuilder
     * @param virtualApplicationsAggregate
     */
    private void updatePropertiesContainer(
                                           final PlatformContainer propertiesBuilder,
                                           final VirtualApplicationsAggregate virtualApplicationsAggregate) {
        final Optional<PlatformData> platform = virtualApplicationsAggregate.getPlatform();

        if (platform.isPresent()) {
            propertiesBuilder.setPlatform(
                    platform.get());

            propertiesBuilder.addProperties(
                    virtualApplicationsAggregate.getProperties());
        }
    }

    /**
     * Return all plaform key for application.
     *
     * @return list of platform key
     */
    public List<PlatformData> getAllPlatform() {
        final List<PlatformData> listPlatform;
        // Redis key pattern to search all application platform
        final String redisKey;
        // To rebuild event
        final VirtualApplicationsAggregate virtualApplicationsAggregate = new VirtualApplicationsAggregate(this.store);

        getLogger().debug("Load all platforms keys for all application.");

        redisKey = String.format("%s-*",
                getStreamPrefix());

        // All application platform redis key.
        final Set<String> platforms = this.store.getStreamsLike(redisKey);

        listPlatform = new ArrayList<>(platforms.size());

        PlatformData platform;
        Iterator<PlatformData> itPlatform;

        for (String platformRedisKey : platforms) {
            virtualApplicationsAggregate.clear();

            // First event is always create event.
            // That mean with you don't need replay event to know with platform is associate to redis stream
            virtualApplicationsAggregate.replay(platformRedisKey, 0, 1);

            itPlatform = virtualApplicationsAggregate.getAllPlatforms().iterator();

            if (itPlatform.hasNext()) {
                platform = itPlatform.next();

                listPlatform.add(platform);
            }
        }

        getLogger().debug("All platform keys for all application are loaded.");

        return listPlatform;
    }

    /**
     * Generate key to search in database.
     *
     * @param ptfKey namespace of module
     *
     * @return db key
     */
    private String generateDbKey(final PlatformKey ptfKey) {
        return String.format("%s-%s",
                getStreamPrefix(), ptfKey.getEntityName());
    }

    /**
     * Store object in snapshot.
     *
     * @param platformKey key of cache (same as cache.get(K))
     * @param object object
     */
    public void saveSnapshot(final PlatformKey platformKey, final PlatformContainer object) {
        final String redisKey = generateDbKey(platformKey);

        // Now store snapshot
        store.storeSnapshot(redisKey, object, nbEventBeforePersiste);
    }

    /**
     * Store object in snapshot.
     *
     * @param platformKey key of cache (same as cache.get(K))
     * @param object object
     */
    public void forceSaveSnapshot(final PlatformKey platformKey, final PlatformContainer object, final long nbEvent) {
        final String redisKey = generateDbKey(platformKey);

        // Now store snapshot
        store.createSnapshot(redisKey, object, nbEvent);
    }
}
