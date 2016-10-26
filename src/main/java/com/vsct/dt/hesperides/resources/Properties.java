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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        checkDuplicateKeyInSet(keyValueProperties);
        checkDuplicateIterableSet(iterableProperties);

        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    /**
     * Check if duplicate key exists.
     *
     * @param iterableProperties set iterable properties
     */
    private static void checkDuplicateIterableSet(
            final Set<IterableValorisation> iterableProperties) {

        // Clear duplicate key
        final Map<String, Valorisation> presentKeys = new HashMap<>();

        iterableProperties.stream().forEach(prop -> {
            if (presentKeys.containsKey(prop.getName())) {
                throw new DuplicateResourceException(String.format("Duplicate input key '%s'", prop.getName()));
            }

            presentKeys.put(prop.getName(), prop);

            prop.getIterableValorisationItems().stream().forEach(item -> {
                checkDuplicateKeyInSet(item.getValues());
            });
        });
    }

    /**
     * Check if duplicate key exists.
     *
     * @param keyValueProperties set of properties
     */
    private static void checkDuplicateKeyInSet(
            final Set<? extends Valorisation> keyValueProperties) {
        // Clear duplicate key
        final Map<String, Valorisation> presentKeys = new HashMap<>();

        keyValueProperties.stream().forEach(k -> {
            if (k instanceof IterableValorisation) {
                final IterableValorisation iv = (IterableValorisation) k;

                checkDuplicateIterableSet(ImmutableSet.of(iv));
            } else if (presentKeys.containsKey(k.getName())) {
                throw new DuplicateResourceException(String.format("Duplicate input key '%s'", k.getName()));
            }

            presentKeys.put(k.getName(), k);
        });
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
