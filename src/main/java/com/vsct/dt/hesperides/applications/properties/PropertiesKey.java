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

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public class PropertiesKey {
    /**
     * Name of application.
     */
    private String applicationName;

    /**
     * Name of platform.
     */
    private String platformName;

    /**
     * Path of properties (box name with '#').
     */
    private String path;

    /**
     *
     * @param applicationName Name of application
     * @param platformName Name of platform
     * @param path Path of properties (box name with '#')
     */
    public PropertiesKey(final String applicationName, final String platformName, final String path) {
        this.applicationName = applicationName;
        this.platformName = platformName;
        this.path = path;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertiesKey)) return false;

        PropertiesKey that = (PropertiesKey) o;

        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
            return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (platformName != null ? !platformName.equals(that.platformName) : that.platformName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationName != null ? applicationName.hashCode() : 0;
        result = 31 * result + (platformName != null ? platformName.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
