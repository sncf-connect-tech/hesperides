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

package com.vsct.dt.hesperides.templating.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Collection;
import java.util.Objects;

/**
 * Value Object to share application to other aggregates
 * Created by william_montaz on 10/12/2014.
 */
@JsonSnakeCase
public final class ApplicationData {

    private final String name;
    private final ImmutableList<PlatformData> platforms;

    @JsonCreator
    public ApplicationData(
            @JsonProperty("name") final String name,
            @JsonProperty("platforms") final Collection<PlatformData> platforms) {
        this.name = name;
        this.platforms = ImmutableList.copyOf(platforms);
    }

    public String getName() {
        return name;
    }

    public ImmutableList<PlatformData> getPlatforms() {
        return platforms;
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, platforms);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ApplicationData other = (ApplicationData) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.platforms, other.platforms);
    }
}
