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
