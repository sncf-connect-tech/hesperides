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

package org.hesperides.presentation.legacydtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by william_montaz on 10/12/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy"})
public final class Techno {

    @JsonProperty("version")
    private final String version;

    @JsonProperty("working_copy")
    private final boolean workingCopy;

    @JsonProperty("name")
    private final String name;

    @JsonCreator
    public Techno(@JsonProperty("name") final String name,
                  @JsonProperty("version") final String version,
                  @JsonProperty("working_copy") final boolean isWorkingCopy) {
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Techno)) return false;

        Techno that = (Techno) o;

        if (workingCopy != that.workingCopy) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (workingCopy ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
