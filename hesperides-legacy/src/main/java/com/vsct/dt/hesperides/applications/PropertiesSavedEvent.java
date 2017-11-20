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
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

/**
 * Created by william_montaz on 13/10/2014.
 *
 * Modified by tidiane_sidibe on 05/08/2016 : adding the comment to event
 */
public final class PropertiesSavedEvent {
    private final String     applicationName;
    private final String     platformName;
    private final String     path;
    private final PropertiesData properties;
    private final String    comment;

    @JsonCreator
    public PropertiesSavedEvent(@JsonProperty("applicationName")final String applicationName,
                                @JsonProperty("platformName") final String platformName,
                                @JsonProperty("path") final String path,
                                @JsonProperty("properties") final PropertiesData properties,
                                @JsonProperty("comment") final String comment) {
        this.applicationName = applicationName;
        this.platformName = platformName;
        this.path = path;
        this.properties = properties;
        this.comment = comment;
    }

    public PropertiesData getProperties() {
        return properties;
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

    public String getComment (){
        return comment;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertiesSavedEvent)) return false;

        PropertiesSavedEvent that = (PropertiesSavedEvent) o;

        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
            return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (platformName != null ? !platformName.equals(that.platformName) : that.platformName != null) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationName != null ? applicationName.hashCode() : 0;
        result = 31 * result + (platformName != null ? platformName.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }
}
