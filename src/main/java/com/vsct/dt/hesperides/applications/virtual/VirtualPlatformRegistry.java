package com.vsct.dt.hesperides.applications.virtual;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.PlatformTimelineKey;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.*;

/**
 * Created by emeric_martineau on 27/05/2016.
 */
public class VirtualPlatformRegistry implements PropertiesRegistryInterface, PlatformRegistryInterface {
    /**
     * Current platform.
     */
    private PlatformData platform;

    /**
     * Current properties.
     */
    private Map<String, PropertiesData> properties = new HashMap<>();

    @Override
    public Optional<PlatformData> getPlatform(final PlatformKey key) {
        return Optional.ofNullable(this.platform);
    }

    @Override
    public Optional<PlatformData> getPlatform(final PlatformTimelineKey key) {
        return Optional.ofNullable(this.platform);
    }

    @Override
    public void createOrUpdatePlatform(final PlatformData platform) {
        this.platform = platform;
    }

    @Override
    public List<PlatformData> getPlatformsForApplication(final String applicationName) {
        final List<PlatformData> list = new ArrayList<>(1);

        if (this.platform != null) {
            list.add(this.platform);
        }

        return list;
    }

    @Override
    public void deletePlatform(final PlatformKey key) {
        this.platform = null;
    }

    @Override
    public Collection<PlatformData> getAllPlatforms() {
        return getPlatformsForApplication(null);
    }

    @Override
    public void removeFromCache(final PlatformKey key) {
        // Nothing
    }

    @Override
    public Optional<PropertiesData> getProperties(final String applicationName, final String platformName, final String path) {
        return Optional.ofNullable(this.properties.get(path));
    }

    @Override
    public Optional<PropertiesData> getProperties(final String applicationName, final String platformName,
                                                  final String path, final long timestamp) {
        return getProperties(applicationName, platformName, path);
    }

    @Override
    public Map<String, PropertiesData> getProperties(final String fromApplication, final String fromPlatform) {
        final Map<String, PropertiesData> newHashMap = new HashMap<>();
        newHashMap.putAll(this.properties);

        return newHashMap;
    }

    @Override
    public void createOrUpdateProperties(final String applicationName, final String platformName, final String path,
                                         final PropertiesData entity) {
        this.properties.put(path, entity);
    }

    @Override
    public void removeFromCache(final String applicationName, final String platformName) {
        // nothing
    }

    @Override
    public void removeAllCache() {
        // nothing
    }

    public void addProperties(final Map<String, PropertiesData> prop) {
        this.properties.putAll(prop);
    }

    public void clear() {
        this.platform = null;
        this.properties.clear();
    }
}
