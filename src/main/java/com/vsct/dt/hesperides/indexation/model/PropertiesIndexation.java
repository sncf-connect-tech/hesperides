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
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;
import java.util.Set;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"id", "hesnamespace", "key_value_properties", "iterable_properties"})
public final class PropertiesIndexation extends Data {

    private final String namespace;
    private final Set<KeyValuePropertyIndexation> keyValueProperties;
    private final Set<IterablePropertyIndexation> iterableProperties;

    @JsonCreator
    public PropertiesIndexation(@JsonProperty("hesnamespace") final String namespace,
                                @JsonProperty("key_value_properties") final Set<KeyValuePropertyIndexation> keyValueProperties,
                                @JsonProperty("iterable_properties") final Set<IterablePropertyIndexation> iterableProperties) {
        this.namespace = namespace;
        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    @JsonProperty(value = "hesnamespace") //namespace is reserved keyword for elasticsearch, so we use hesnamespace instead
    public String getNamespace() {
        return namespace;
    }

    public Set<KeyValuePropertyIndexation> getKeyValueProperties() {
        return Sets.newHashSet(keyValueProperties);
    }

    public Set<IterablePropertyIndexation> getIterableProperties() {
        return Sets.newHashSet(iterableProperties);
    }


    /*
     *
     * TOSTRING, EQUALS, HASHCODE
     *
     */

    @Override
    public String toString() {
        return "HesperidesProperties{" +
                "namespace='" + namespace + '\'' +
                ", keyValueProperties=" + Joiner.on(", ").join(keyValueProperties) +
                ", iterableProperties=" + Joiner.on(", ").join(iterableProperties) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, keyValueProperties, iterableProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PropertiesIndexation other = (PropertiesIndexation) obj;
        return Objects.equals(this.namespace, other.namespace)
                && Objects.equals(this.keyValueProperties, other.keyValueProperties)
                && Objects.equals(this.iterableProperties, other.iterableProperties);
    }

    @Override
    protected int getKey() {
        return Objects.hash(namespace);
    }
}
