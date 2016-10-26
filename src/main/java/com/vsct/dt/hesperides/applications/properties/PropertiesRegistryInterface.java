package com.vsct.dt.hesperides.applications.properties;

import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.Optional;

/**
 * Created by emeric_martineau on 27/04/2016.
 */
public interface PropertiesRegistryInterface {
    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @return The properties entity or empty
     */
    Optional<PropertiesData> getProperties(String applicationName, String platformName,
                                                  String path);

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @param timestamp
     *
     * @return The properties entity or empty
     */
    Optional<PropertiesData> getProperties(String applicationName, String platformName,
                                                  String path, long timestamp);

    /**
     * @param fromApplication
     * @param fromPlatform
     * @return
     */
    Map<String, PropertiesData> getProperties(String fromApplication, String fromPlatform);

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @param entity
     */
    void createOrUpdateProperties(String applicationName, String platformName, String path,
                                         PropertiesData entity);

    /**
     * Remove item from cache.
     *
     * @param applicationName
     * @param platformName
     */
    void removeFromCache(String applicationName, String platformName);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
