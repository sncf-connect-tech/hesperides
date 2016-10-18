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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.Objects;

/**
 * Created by william_montaz on 22/04/2015.
 */
public class PlatformSnapshot {
    private final PlatformData platform;
    private final Map<String, PropertiesData> properties;

    @JsonCreator
    public PlatformSnapshot(@JsonProperty("platform") PlatformData platform,
                            @JsonProperty("properties") Map<String, PropertiesData> properties) {
        this.platform = platform;
        this.properties = Maps.newHashMap(properties);
    }

    public PlatformData getPlatform() {
        return platform;
    }

    public Map<String, PropertiesData> getProperties() {
        return Maps.newHashMap(properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlatformSnapshot other = (PlatformSnapshot) obj;
        return Objects.equals(this.platform, other.platform)
                && Objects.equals(this.properties, other.properties);
    }

}
