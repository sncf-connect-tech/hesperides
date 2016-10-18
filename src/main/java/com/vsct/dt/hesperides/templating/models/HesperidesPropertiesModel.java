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

package com.vsct.dt.hesperides.templating.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.Code;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;
import java.util.Set;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public final class HesperidesPropertiesModel {
    private final Set<KeyValuePropertyModel> keyValueProperties;
    private final Set<IterablePropertyModel> iterableProperties;

    private HesperidesPropertiesModel() {
        this.keyValueProperties = Sets.newHashSet();
        this.iterableProperties = Sets.newHashSet();
    }

    public HesperidesPropertiesModel(final Code[] codes) {
        this.keyValueProperties = Sets.newHashSet();
        this.iterableProperties = Sets.newHashSet();
        for (final Code code : codes) {
            if (code instanceof ValueCode) {
                this.addKeyValue((ValueCode) code);
            } else if (code instanceof IterableCode) {
                this.addIterable((IterableCode) code);
            }
        }
    }

    @JsonCreator
    public HesperidesPropertiesModel(@JsonProperty("key_value_properties") final Set<KeyValuePropertyModel> keyValueProperties,
                                     @JsonProperty("iterable_properties") final Set<IterablePropertyModel> iterableProperties) {
        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    public static HesperidesPropertiesModel empty() {
        return new HesperidesPropertiesModel();
    }

    public boolean hasProperty(final String name) {
        for (final Property property : iterableProperties) {
            if (property.getName().equals(name)) return true;
        }
        for (final Property property : keyValueProperties) {
            if (property.getName().equals(name)) return true;
        }
        return false;
    }

    public HesperidesPropertiesModel merge(final HesperidesPropertiesModel modelToMerge) {
        HesperidesPropertiesModel merged = new HesperidesPropertiesModel(this.keyValueProperties,this.iterableProperties);
        merged.keyValueProperties.addAll(modelToMerge.getKeyValueProperties());
        /* MAYBE FIND SOME CLEVER WAY IF NEEDED */
        merged.iterableProperties.addAll(modelToMerge.getIterableProperties());
        return merged;
    }

    /* Should be used only in constructor to ensure immutability */
    private void addIterable(final IterableCode code) {
        this.iterableProperties.add(new IterablePropertyModel(code));
    }

    /* Should be used only in constructor to ensure immutability */
    private void addKeyValue(final ValueCode code) {
        this.keyValueProperties.add(new KeyValuePropertyModel(code));
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
        final HesperidesPropertiesModel other = (HesperidesPropertiesModel) obj;
        return Objects.equals(this.keyValueProperties, other.keyValueProperties)
                && Objects.equals(this.iterableProperties, other.iterableProperties);
    }

    public Set<KeyValuePropertyModel> getKeyValueProperties() {
        return Sets.newHashSet(keyValueProperties);
    }

    public Set<IterablePropertyModel> getIterableProperties() {
        return Sets.newHashSet(iterableProperties);
    }

}

