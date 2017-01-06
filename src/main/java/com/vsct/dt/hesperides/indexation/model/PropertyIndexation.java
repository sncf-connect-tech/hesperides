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

package com.vsct.dt.hesperides.indexation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by william_montaz on 11/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"name", "comment"})
public class PropertyIndexation {

    private final String comment;
    private final String name;

    @JsonCreator
    public PropertyIndexation(@JsonProperty("name") final String name,
                              @JsonProperty("comment") final String comment) {
        this.name = name;
        this.comment = comment;
    }

    public final String getComment() {
        return comment;
    }

    public final String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PropertyIndexation other = (PropertyIndexation) obj;
        return Objects.equals(this.comment, other.comment)
                && Objects.equals(this.name, other.name);
    }
}
