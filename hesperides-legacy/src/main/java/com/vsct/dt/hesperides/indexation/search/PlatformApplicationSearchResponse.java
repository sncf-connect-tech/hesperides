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
 *
 */

package com.vsct.dt.hesperides.indexation.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Value Object representing the response of an elasticsearch for application
 * Created by william_montaz on 29/10/2014.
 */
public final class PlatformApplicationSearchResponse {

    private final String applicationName;

    private final String platformName;

    @JsonCreator
    public PlatformApplicationSearchResponse(@JsonProperty("application_name") final String applicationName,
                                             @JsonProperty("platform_name") final String platformName) {
        this.applicationName = applicationName;
        this.platformName = platformName;
    }

    @JsonProperty(value = "application_name")
    public String getApplicationName() {
        return applicationName;
    }

    @JsonProperty(value = "platform_name")
    public String getPlatformName() {
        return platformName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, platformName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlatformApplicationSearchResponse other = (PlatformApplicationSearchResponse) obj;
        return Objects.equals(this.applicationName, other.applicationName)
                && Objects.equals(this.platformName, other.platformName);
    }
}
