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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by emeric_martineau on 26/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "key_values"})
public class InstanceData {
    private static final String VALORISATION_KEY_INSTANCE_NAME = "hesperides.instance.name";

    private String                    name;
    private Set<KeyValueValorisationData> keyValues;

    private InstanceData() {
        // Nothing
    }

    @JsonCreator
    protected InstanceData(@JsonProperty("name") final String name,
                    @JsonProperty("key_values") final Set<KeyValueValorisationData> keyValues) {
        this.name = name;
        this.keyValues = Sets.newHashSet(keyValues);
    }

    public String getName() {
        return name;
    }

    public Set<KeyValueValorisationData> getKeyValues() {
        return keyValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof InstanceData)) return false;

        InstanceData that = (InstanceData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HesperideInstance{" +
                "name='" + name + '\'' +
                '}';
    }

    public Set<KeyValueValorisationData> generatePredefinedScope() {
        /* KeyValues set for the instance + an automatically set key value holding the instance name */
        Set<KeyValueValorisationData> keyValues = new HashSet<>();
        keyValues.add(new KeyValueValorisationData(VALORISATION_KEY_INSTANCE_NAME, this.name));
        return keyValues;
    }

    public static IApplicationName withInstanceName(final String name) {
        return new Builder(name);
    }

    public interface IApplicationName {
        IBuilder withKeyValue(Set<KeyValueValorisationData> keyValues);
    }

    public interface IBuilder {
        InstanceData build();
    }

    public static class Builder implements IBuilder, IApplicationName {
        private InstanceData instance = new InstanceData();

        public Builder(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Instance name contain wrong character");

            instance.name = name;
        }

        @Override
        public IBuilder withKeyValue(final Set<KeyValueValorisationData> keyValues) {
            checkNotNull(keyValues, "Modules should not be null");
            // TODO emeric : can be empty ?
            //checkArgument(!keyValues.isEmpty(), "Modules should not be empty");

            instance.keyValues = ImmutableSet.copyOf(keyValues);
            return this;
        }

        @Override
        public InstanceData build() {
            return instance;
        }
    }
}
