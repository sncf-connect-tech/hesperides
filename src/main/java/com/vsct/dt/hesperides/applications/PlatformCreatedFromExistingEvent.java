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
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;

/**
 * Created by tidiane_sidibe on 11/08/2016.
 *
 */

//This could inherit from PlatformCreatedEvent if it was not a final class
public final class PlatformCreatedFromExistingEvent {

    private final String   applicationName;
    private final PlatformData platform;

    private final PlatformData originPlatform;
    private final Map<String, PropertiesData> originProperties;

    @JsonCreator
    public PlatformCreatedFromExistingEvent(@JsonProperty("applicationName") final String applicationName,
                                            @JsonProperty("platform") final PlatformData platform,
                                            @JsonProperty("originPlatform") final PlatformData originPlatform,
                                            @JsonProperty("originProperties") final Map<String, PropertiesData> originProperties) {
        this.platform = platform;
        this.applicationName = applicationName;

        this.originPlatform = originPlatform;
        this.originProperties = originProperties;
    }

    public PlatformData getPlatform() {
        return platform;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public PlatformData getOriginPlatform (){
        return originPlatform;
    }

    public Map<String, PropertiesData> getOriginProperties (){
        return originProperties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformCreatedFromExistingEvent)) return false;

        PlatformCreatedFromExistingEvent that = (PlatformCreatedFromExistingEvent) o;

        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
            return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationName != null ? applicationName.hashCode() : 0;
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        return result;
    }
}
