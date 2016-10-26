package com.vsct.dt.hesperides.applications;

import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by emeric_martineau on 27/04/2016.
 */
public interface PlatformRegistryInterface {
    Optional<PlatformData> getPlatform(PlatformKey key);

    Optional<PlatformData> getPlatform(PlatformTimelineKey key);

    void createOrUpdatePlatform(PlatformData platform);

    List<PlatformData> getPlatformsForApplication(String applicationName);

    void deletePlatform(PlatformKey key);

    Collection<PlatformData> getAllPlatforms();

    /**
     * Remove item from cache.
     *
     * @param key
     */
    void removeFromCache(PlatformKey key);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
