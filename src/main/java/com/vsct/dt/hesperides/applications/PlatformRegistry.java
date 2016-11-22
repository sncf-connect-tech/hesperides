/*
 *
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
 *
 */

package com.vsct.dt.hesperides.applications;

import com.google.common.cache.LoadingCache;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.applications.properties.cache.PropertiesCacheLoader;
import com.vsct.dt.hesperides.applications.properties.cache.PropertiesTimelineCacheLoader;
import com.vsct.dt.hesperides.applications.properties.event.PlatformContainer;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.util.HesperidesCacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This platform registry holds everything inmemory.
 * It might be updated for further development in order to manage memory in a better and safier way
 */
class PlatformRegistry implements PropertiesRegistryInterface, PlatformRegistryInterface {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformRegistry.class);

    /**
     * Little cache to retain platform loaded at a specific point in time
     * It is very likely for users to perform read operations on that platform once they loaded it
     */
    private final LoadingCache<PlatformTimelineKey, PlatformContainer> cacheTimelineProperties;

    /**
     * Cache contain applicaiton or load it.
     */
    private final LoadingCache<PlatformKey, PlatformContainer> cache;

    /**
     * Cache loader.
     */
    private final PropertiesCacheLoader propertiesCacheLoader;

    /**
     * Constructor.
     *
     * @param store store of event for lazy load
     * @param nbEventBeforePersiste nb event before save
     * @param config config of cache for platform
     * @param configTimeLine config of cache for platform in the past
     */
    public PlatformRegistry(final EventStore store,
                            final long nbEventBeforePersiste,
                            final HesperidesCacheParameter config,
                            final HesperidesCacheParameter configTimeLine) {


        this.propertiesCacheLoader = new PropertiesCacheLoader(store, nbEventBeforePersiste);

        this.cacheTimelineProperties = HesperidesCacheBuilder.newBuilder(configTimeLine, (key, value) -> ((PlatformContainer) value).getProperties().size())
                .build(new PropertiesTimelineCacheLoader(store, nbEventBeforePersiste));

        this.cache = HesperidesCacheBuilder.newBuilder(config)
                .build(this.propertiesCacheLoader);
    }

    @Override
    public Optional<PlatformData> getPlatform(final PlatformKey key) {
        try {
            LOGGER.debug("Search platform by key '{}'.", key);

            return Optional.ofNullable(this.cache.get(key).getPlatform());
        } catch (final ExecutionException e) {
            LOGGER.debug("Platform (key '{}' not found !", key);

            // Platform not found
            return Optional.empty();
        }
    }

    @Override
    public Optional<PlatformData> getPlatform(final PlatformTimelineKey key) {
        try {
            LOGGER.debug("Search in past platform by key '{}'.", key);

            return Optional.ofNullable(this.cacheTimelineProperties.get(key).getPlatform());
        } catch (final ExecutionException e) {
            LOGGER.debug("Platform in past (key '{}' not found !", key);

            // Platform not found
            return Optional.empty();
        }
    }

    @Override
    public void createOrUpdatePlatform(final PlatformData platform) {
        LOGGER.debug("Add platform '{}'", platform);

        PlatformContainer platformContainer;

        try {
            platformContainer = this.cache.get(platform.getKey());
        } catch (final ExecutionException e) {
            // When not found in database -> create
            platformContainer = new PlatformContainer();
        }

        // Update module
        platformContainer.setPlatform(platform);

        // Write snapshot
        this.propertiesCacheLoader.saveSnapshot(platform.getKey(), platformContainer);
    }

    @Override
    public List<PlatformData> getPlatformsForApplication(final String applicationName) {
        LOGGER.debug("Get all platforms for application '{}'", applicationName == null ? "all" : applicationName);

        // Get all key of platform
        final List<PlatformKey> platformKey = this.propertiesCacheLoader.getAllPlatformKeyFromApplication(applicationName);
        // Create result list to return to this method
        final List<PlatformData> listCachePlatform = new ArrayList<>(platformKey.size());

        // Filter key if platform is loaded
        final List<PlatformKey> platformKeyToLoad = platformKey.stream().filter(key -> {
            final PlatformContainer platformContainer = this.cache.getIfPresent(key);

            final boolean notExists = platformContainer == null;

            if (!notExists) {
                listCachePlatform.add(platformContainer.getPlatform());
            }

            return notExists;
        }).collect(Collectors.toList());

        // Get platform
        final Map<PlatformKey, PlatformContainer> list
                = this.propertiesCacheLoader.getPlatformFromApplication(platformKeyToLoad);

        for (Map.Entry<PlatformKey, PlatformContainer> entryPlatform : list.entrySet()) {
            LOGGER.debug("Platform '{}' is missing in cache, put it.", entryPlatform.getKey());

            this.cache.put(entryPlatform.getKey(), entryPlatform.getValue());
        }

        // Convert Map to list
        final List<PlatformData> missingCachePlatform = list.entrySet().stream()
                .map(platformKeyPlatformContainerEntry -> platformKeyPlatformContainerEntry.getValue().getPlatform())
                .collect(Collectors.toList());

        listCachePlatform.addAll(missingCachePlatform);

        return listCachePlatform.stream().filter(p -> p != null)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePlatform(final PlatformKey key) {
        try {
            PlatformContainer platformContainer;

            // Load snapshot
            platformContainer = this.cache.get(key);

            // Update module
            platformContainer.setPlatform(null);

            // Write snapshot
            this.propertiesCacheLoader.saveSnapshot(key, platformContainer);
        } catch (final ExecutionException e) {
            // ??? normaly not !
        }
    }

    @Override
    public Collection<PlatformData> getAllPlatforms() {
        LOGGER.debug("Get all platform.");

        // Return all platform
        return getPlatformsForApplication(null);
    }

    @Override
    public void removeFromCache(final PlatformKey key) {
        this.cache.invalidate(key);
    }


    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @return The properties entity or empty
     */
    @Override
    public Optional<PropertiesData> getProperties(final String applicationName, final String platformName,
                                                  final String path) {
        try {
            LOGGER.debug("Search properties for application '{}' on platform '{}' with path '{}'.",
                    applicationName, platformName, path);

            final PlatformKey platformKey = new PlatformKey(applicationName, platformName);
            final PlatformContainer platformContainer = this.cache.get(platformKey);
            final PropertiesData prop = platformContainer.getProperties().get(path);

            return Optional.ofNullable(prop);
        } catch (final ExecutionException e) {
            LOGGER.debug("Properties for application '{}' on platform '{}' with path '{}' not found !",
                    applicationName, platformName, path);

            // Platform not found
            return Optional.empty();
        }
    }

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @param timestamp
     *
     * @return The properties entity or empty
     */
    @Override
    public Optional<PropertiesData> getProperties(final String applicationName, final String platformName,
                                                  final String path, final long timestamp) {
        try {
            if (LOGGER.isDebugEnabled()) {
                final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                final Date resultdate = new Date(timestamp);

                LOGGER.debug("Search properties for application '{}' on platform '{}' with path '{}' at date {}.",
                        applicationName, platformName, path, sdf.format(resultdate));
            }

            final PlatformTimelineKey platformKey = new PlatformTimelineKey(new PlatformKey(applicationName, platformName), timestamp);
            final PlatformContainer platformContainer = this.cacheTimelineProperties.get(platformKey);
            final PropertiesData prop = platformContainer.getProperties().get(path);

            return Optional.ofNullable(prop);
        } catch (final ExecutionException e) {
            if (LOGGER.isDebugEnabled()) {
                final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                final Date resultdate = new Date(timestamp);

                LOGGER.debug("Properties for application '{}' on platform '{}' with path '{}' at date {} not found !.",
                        applicationName, platformName, path, sdf.format(resultdate));
            }

            // Platform not found
            return Optional.empty();
        }
    }

    /**
     * @param fromApplication
     * @param fromPlatform
     * @return
     */
    @Override
    public Map<String, PropertiesData> getProperties(String fromApplication, String fromPlatform) {
        try {
            LOGGER.debug("Get all properties for application '{}' on platform '{}'.", fromApplication, fromPlatform);

            final PlatformKey platformKey = new PlatformKey(fromApplication, fromPlatform);
            final PlatformContainer platformContainer = this.cache.get(platformKey);
            final Map<String, PropertiesData> prop = platformContainer.getProperties();

            final Map<String, PropertiesData> newHashMap = new HashMap<>();
            newHashMap.putAll(prop);

            return newHashMap;
        } catch (final ExecutionException e) {
            LOGGER.debug("All properties for application '{}' on platform '{}' not found.",
                    fromApplication, fromPlatform);

            // Platform not found
            return new HashMap<>();
        }
    }

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @param entity
     */
    @Override
    public void createOrUpdateProperties(final String applicationName, final String platformName, final String path,
                                         final PropertiesData entity) {
        try {
            LOGGER.debug("Add or update properties for application '{}' on platform '{}' with path '{}'.",
                    applicationName, platformName, path);

            final PlatformKey platformKey = new PlatformKey(applicationName, platformName);
            final PlatformContainer platformContainer = this.cache.get(platformKey);
            platformContainer.getProperties().put(path, entity);

            this.propertiesCacheLoader.saveSnapshot(platformKey, platformContainer);
        } catch (final ExecutionException e) {
            LOGGER.debug("Can't add or update properties for application '{}' on platform '{}' with path '{}'.",
                    applicationName, platformName, path);
            // Platform not found
        }
    }

    @Override
    public void removeFromCache(final String applicationName, final String platformName) {
        this.cache.invalidate(new PlatformKey(applicationName, platformName));
    }

    @Override
    public void removeAllCache() {
        this.cache.invalidateAll();
    }
}
