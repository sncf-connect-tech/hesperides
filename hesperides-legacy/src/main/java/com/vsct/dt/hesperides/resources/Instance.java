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

package com.vsct.dt.hesperides.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Set;

/**
 * Created by william_montaz on 14/08/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "key_values"})
public final class Instance {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("key_values")
    private final Set<KeyValueValorisation> keyValues;

    private Instance(final String name) {
        this.name = name;
        this.keyValues = Sets.newHashSet();
    }

    @JsonCreator
    public Instance(@JsonProperty("name") final String name,
                    @JsonProperty("key_values") final Set<KeyValueValorisation> keyValues) {
        this.name = name;
        this.keyValues = Sets.newHashSet(keyValues);
    }

    public static Instance empty(final String name) {
        return new Instance(name);
    }

    public String getName() {
        return name;
    }

    public Set<KeyValueValorisation> getKeyValues() {
        return Sets.newHashSet(keyValues);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Instance)) return false;

        Instance that = (Instance) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InstanceVO{" +
                "name='" + name + '\'' +
                '}';
    }
}
