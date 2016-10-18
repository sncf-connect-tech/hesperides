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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"key_value_properties", "iterable_properties"})
public final class Properties {

    @JsonProperty("key_value_properties")
    private final Set<KeyValueValorisation> keyValueProperties;

    @JsonProperty("iterable_properties")
    private final Set<IterableValorisation> iterableProperties;

    @JsonCreator
    public Properties(@JsonProperty("key_value_properties") final Set<KeyValueValorisation> keyValueProperties,
                      @JsonProperty("iterable_properties") final Set<IterableValorisation> iterableProperties) {
        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    public static Properties empty() {
        return new Properties(Sets.newHashSet(), Sets.newHashSet());
    }

    public Properties makeCopyWithoutNullOrEmptyValorisations() {
        Set<KeyValueValorisation> keyValuePropertiesCleaned = this.keyValueProperties
                .stream()
                .filter(kvp -> kvp.getValue() != null && !kvp.getValue().isEmpty())
                .collect(Collectors.toSet());

        //don't handle iterable properties for now
        return new Properties(keyValuePropertiesCleaned, this.getIterableProperties());
    }

    public Set<KeyValueValorisation> getKeyValueProperties() {
        return Sets.newHashSet(keyValueProperties);
    }

    public Set<IterableValorisation> getIterableProperties() {
        return Sets.newHashSet(iterableProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyValueProperties, iterableProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Properties other = (Properties) obj;
        return Objects.equals(this.keyValueProperties, other.keyValueProperties)
                && Objects.equals(this.iterableProperties, other.iterableProperties);
    }
}
