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

/**
 * Created by william_montaz on 10/12/2014.
 */
public final class PlatformCreatedEvent {
    private final String   applicationName;
    private final PlatformData platform;

    @JsonCreator
    public PlatformCreatedEvent(@JsonProperty("applicationName") final String applicationName,
                                @JsonProperty("platform") final PlatformData platform) {
        this.platform = platform;
        this.applicationName = applicationName;
    }

    public PlatformData getPlatform() {
        return platform;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformCreatedEvent)) return false;

        PlatformCreatedEvent that = (PlatformCreatedEvent) o;

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
