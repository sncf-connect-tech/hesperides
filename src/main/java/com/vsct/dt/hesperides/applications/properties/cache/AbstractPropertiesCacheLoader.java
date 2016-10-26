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
     * @param useSnapshot if read/write snapshot
     *
     * @return properties
     *
     * @throws Exception if not found.
     */
    protected PlatformContainer loadProperties(final PlatformKey key, final long timestamp,
                                               final boolean useSnapshot) throws Exception {
        getLogger().debug("Load properties with key '{}' on timestamp '{}'.", key, timestamp);

        // Redis key pattern to search all application platform
        final String redisKey = generateDbKey(key);

        // Properties builder
        PlatformContainer propertiesBuilder;
        HesperidesSnapshotItem hesperidesSnapshotItem;

        if (useSnapshot) {
            hesperidesSnapshotItem = store.findLastSnapshot(redisKey);
        } else {
            hesperidesSnapshotItem = null;
        }

        if (hesperidesSnapshotItem == null
                || hesperidesSnapshotItem.getCurrentNbEvents() < hesperidesSnapshotItem.getNbEvents()) {
            propertiesBuilder = new PlatformContainer();

            final VirtualApplicationsAggregate virtualApplicationsAggregate = new VirtualApplicationsAggregate(store);

            virtualApplicationsAggregate.replay(redisKey);

            updatePropertiesContainer(propertiesBuilder, virtualApplicationsAggregate);
        } else {
            propertiesBuilder = (PlatformContainer) hesperidesSnapshotItem.getSnapshot();

            if (hesperidesSnapshotItem.getCurrentNbEvents() > hesperidesSnapshotItem.getNbEvents()) {
                final VirtualApplicationsAggregate virtualApplicationsAggregate = new VirtualApplicationsAggregate(
                        store,
                        propertiesBuilder.getPlatform(),
                        propertiesBuilder.getProperties());

                final long start = hesperidesSnapshotItem.getNbEvents();
                final long stop = hesperidesSnapshotItem.getCurrentNbEvents();

                virtualApplicationsAggregate.replay(redisKey, start, stop);

                updatePropertiesContainer(propertiesBuilder, virtualApplicationsAggregate);
            }
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
     * @param applicationName applicationName or null
     *
     * @return list of platform key
     */
    public List<PlatformKey> getAllPlatformKeyFromApplication(final String applicationName) {
        final List<PlatformKey> listPlatformKey;
        // Redis key pattern to search all application platform
        final String redisKey;

        if (applicationName == null) {
            getLogger().debug("Load all platforms keys for all application.");

            redisKey = String.format("%s-*",
                    getStreamPrefix());
        } else {
            getLogger().debug("Load all platforms keys for application '{}' from store.", applicationName);

            redisKey = String.format("%s-%s-*",
                    getStreamPrefix(), applicationName);
        }

        // All application platform redis key.
        final Set<String> platforms = this.store.getStreamsLike(redisKey);

        listPlatformKey = new ArrayList<>(platforms.size());

        for (String platformRedisKey : platforms) {
            listPlatformKey.add(
                    new PlatformKey(
                            entityNameFormRedisKey(platformRedisKey)));
        }


        if (applicationName == null) {
            getLogger().debug("All platform keys for all application are loaded.");
        } else {
            getLogger().debug("All platform keys for application '{}' are loaded.", applicationName);
        }

        return listPlatformKey;
    }

    /**
     * Return list from application name.
     *
     * @param platformKey list of key
     *
     * @return list of application (never return null. Maybe return empty list)
     */
    public Map<PlatformKey, PlatformContainer> getPlatformFromApplication(final List<PlatformKey> platformKey) {
        getLogger().debug("Load platform for {} keys.", platformKey.size());

        // List of platform return by method
        final Map<PlatformKey, PlatformContainer> listPlatform = new HashMap<>(platformKey.size());
        // Current platform
        PlatformContainer currentPlatform;
        // Key of redis
        String platformRedisKey;

        for (PlatformKey ptfKey : platformKey) {
            platformRedisKey = generateDbKey(ptfKey);

            getLogger().debug("Load platform from store associate with key '{}'.", platformRedisKey);

            try {
                currentPlatform = loadProperties(ptfKey, Long.MAX_VALUE, true);
            } catch (Exception e) {
                e.printStackTrace();
                currentPlatform = null;
            }

            // Platform can be remove at last event
            if (currentPlatform != null) {
                listPlatform.put(ptfKey, currentPlatform);
            }
        }

        getLogger().debug("{} platforms loaded.", listPlatform.size());

        return listPlatform;
    }

    /**
     * Generate name of entity.
     *
     * @param redisKey
     * @return
     */
    private String entityNameFormRedisKey(final String redisKey) {
        return redisKey.substring(getStreamPrefix().length() + 1);
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
}
