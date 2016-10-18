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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 11/07/14.
 *
 * WARNING : don't override equals for REST input object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "iterable_valorisation_items"})
@JsonDeserialize//This annotation is important, it prevents IterableValorisation from using the Valorisation Deserializer, which would cause endless loops
public final class IterableValorisation extends Valorisation {
    private final List<IterableValorisationItem> iterableValorisationItems;

    @JsonCreator
    public IterableValorisation(@JsonProperty("name") final String name,
                                @JsonProperty("iterable_valorisation_items") final List<IterableValorisationItem> iterableValorisationItems) {
        super(name);
        Preconditions.checkNotNull(iterableValorisationItems, "iterableValorisationItems field for an iterable valorisation should not be null");
        this.iterableValorisationItems = Lists.newArrayList(iterableValorisationItems);
    }

    public List<IterableValorisationItem> getIterableValorisationItems() {
        return Lists.newArrayList(iterableValorisationItems);
    }

    @Override
    public Valorisation inject(Map<String, String> keyValueProperties) {
        List<IterableValorisationItem> items = this.iterableValorisationItems.stream().map(i -> i.inject(keyValueProperties)).collect(Collectors.toList());
        return new IterableValorisation(this.getName(), items);
    }

    @JsonPropertyOrder({"title", "values"})
    public static class IterableValorisationItem {
        private final String                    title;
        private final Set<Valorisation> values;

        @JsonCreator
        public IterableValorisationItem(@JsonProperty("title") final String title,
                                        @JsonProperty("values") final Set<Valorisation> values) {
            this.title = title;
            this.values = Sets.newHashSet(values);
        }

        public String getTitle() {
            return title;
        }

        public Set<Valorisation> getValues() {
            return Sets.newHashSet(values);
        }

        public IterableValorisationItem inject(Map<String, String> keyValueProperties) {
            Set<Valorisation> newValorisations = values.stream().map(v -> v.inject(keyValueProperties)).collect(Collectors.toSet());
            return new IterableValorisationItem(this.getTitle(), newValorisations);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IterableValorisationItem that = (IterableValorisationItem) o;

            if (title != null ? !title.equals(that.title) : that.title != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result;
            return result;
        }
    }
}
