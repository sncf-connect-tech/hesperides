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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;
import java.util.Set;

/**
 * Created by william_montaz on 14/08/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public final class InstanceModel {

    private final Set<KeyValuePropertyModel> keys;

    @JsonCreator
    public InstanceModel(@JsonProperty("keys") final Set<KeyValuePropertyModel> keyValues) {
        this.keys = Sets.newHashSet(keyValues);
    }

    public Set<KeyValuePropertyModel> getKeys() {
        return Sets.newHashSet(keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final InstanceModel other = (InstanceModel) obj;
        return Objects.equals(this.keys, other.keys);
    }
}
