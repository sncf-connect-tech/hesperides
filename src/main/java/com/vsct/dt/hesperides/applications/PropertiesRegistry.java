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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.Optional;

/**
 * Inner class that helps maintaining the set of properties known by hesperides.
 * It holds everything inmemory.
 * It might be updated for further development in order to manage memory in a better and safier way
 */
final class PropertiesRegistry {

    /**
     * The composite key to find properties in the hashmap.
     */
    private static class Key {
        private String applicationName;
        private String platformName;

        private Key(final String applicationName, final String platformName) {
            this.applicationName = applicationName;
            this.platformName = platformName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (applicationName != null ? !applicationName.equals(key.applicationName) : key.applicationName != null)
                return false;
            if (platformName != null ? !platformName.equals(key.platformName) : key.platformName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = applicationName != null ? applicationName.hashCode() : 0;
            result = 31 * result + (platformName != null ? platformName.hashCode() : 0);
            return result;
        }
    }

    /**
     * The structure holding properties instances.
     * We use a guava Table. Since it is not synchronized, we will synchronized all methods as a first basic implementation without lock management
     * If there really is a concurrency drawback we could think of using a ReentrantReadWriteLock
     */
    private Table<Key, String, PropertiesData> properties = HashBasedTable.create();

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @return The properties entity or empty
     */
    synchronized Optional<PropertiesData> getProperties(final String applicationName, final String platformName, final String path) {
        return Optional.ofNullable(properties.get(new Key(applicationName, platformName), normalizePath(path)));
    }

    /**
     * @param fromApplication
     * @param fromPlatform
     * @return
     */
    synchronized Map<String, PropertiesData> getProperties(String fromApplication, String fromPlatform) {
        return properties.row(new Key(fromApplication, fromPlatform));
    }

    /**
     * @param applicationName
     * @param platformName
     * @param path
     * @param entity
     */
    synchronized void createOrUpdate(final String applicationName, final String platformName, final String path, final PropertiesData entity) {
        properties.put(new Key(applicationName, platformName), normalizePath(path), entity);
    }

    /**
     * @param applicationName
     * @param platformName
     * @param path
     */
    synchronized void delete(final String applicationName, final String platformName, final String path) {
        properties.remove(new Key(applicationName, platformName), normalizePath(path));
    }

    private String normalizePath(String path) {
        StringBuilder builder = new StringBuilder();
        //We ensure that path will always start with # even if it is omitted by the caller
        if (!path.startsWith("#")) {
            builder.append('#');
        }
        builder.append(path);
        return builder.toString();
    }

}
