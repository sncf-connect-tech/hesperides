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

package com.vsct.dt.hesperides.applications.properties.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformContainer {
    /**
     * Current platform.
     */
    @JsonProperty("platform")
    private PlatformData platform;

    /**
     * Current properties.
     */
    @JsonProperty("properties")
    private Map<String, PropertiesData> properties = new ConcurrentHashMap<>();

    @JsonCreator
    public PlatformContainer(@JsonProperty("platform") final PlatformData platform,
                             @JsonProperty("properties") final Map<String, PropertiesData> properties) {
        this.platform = platform;

        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    public PlatformContainer() {
        /// Nothing
    }

    /**
     * Return platform.
     *
     * @return platform.
     */
    public PlatformData getPlatform() {
        return this.platform;
    }

    public Map<String, PropertiesData> getProperties() {
        return this.properties;
    }

    public void setPlatform(final PlatformData platform) {
        this.platform = platform;
    }

    public void addProperties(final Map<String, PropertiesData> properties) {
        this.properties.putAll(properties);
    }
}
