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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.MustacheScope;
import com.vsct.dt.hesperides.applications.MustacheScopeEntry;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 11/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "iterable_valorisation_items"})
@JsonDeserialize//This annotation is important, it prevents IterableValorisation from using the Valorisation Deserializer, which would cause endless loops
public final class IterableValorisationData extends ValorisationData {
    private final List<IterableValorisationItemData> iterableValorisationItems;

    @JsonCreator
    public IterableValorisationData(@JsonProperty("name") final String name,
                                    @JsonProperty("iterable_valorisation_items") final List<IterableValorisationItemData> iterableValorisationItems) {
        super(name);
        Preconditions.checkNotNull(iterableValorisationItems, "iterableValorisationItems field for an iterable valorisation should not be null");
        this.iterableValorisationItems = Lists.newArrayList(iterableValorisationItems);
    }

    public List<IterableValorisationItemData> getIterableValorisationItems() {
        return Lists.newArrayList(iterableValorisationItems);
    }

    @Override
    public MustacheScopeEntry<String, Object> toMustacheScopeEntry() {
        List<MustacheScope> innerScopes = Lists.newArrayList();
        for(IterableValorisationItemData item: iterableValorisationItems){
            innerScopes.add(new MustacheScope(item.getValues()));
        }
        return new MustacheScopeEntry<>(this.getName(), innerScopes);
    }

    @Override
    public ValorisationData inject(Map<String, String> keyValueProperties) {
        List<IterableValorisationItemData> items = this.iterableValorisationItems.stream().map(i -> i.inject(keyValueProperties)).collect(Collectors.toList());
        return new IterableValorisationData(this.getName(), items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IterableValorisationData that = (IterableValorisationData) o;

        if (iterableValorisationItems != null ? !iterableValorisationItems.equals(that.iterableValorisationItems) : that.iterableValorisationItems != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (iterableValorisationItems != null ? iterableValorisationItems.hashCode() : 0);
        return result;
    }

    @JsonPropertyOrder({"title", "values"})
    public static class IterableValorisationItemData {
        private final String                    title;
        private final Set<ValorisationData> values;

        @JsonCreator
        public IterableValorisationItemData(@JsonProperty("title") final String title,
                                        @JsonProperty("values") final Set<ValorisationData> values) {
            this.title = title;
            this.values = Sets.newHashSet(values);
        }

        public String getTitle() {
            return title;
        }

        public Set<ValorisationData> getValues() {
            return Sets.newHashSet(values);
        }

        public IterableValorisationItemData inject(Map<String, String> keyValueProperties) {
            Set<ValorisationData> newValorisations = values.stream().map(v -> v.inject(keyValueProperties)).collect(Collectors.toSet());
            return new IterableValorisationItemData(this.getTitle(), newValorisations);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IterableValorisationItemData that = (IterableValorisationItemData) o;

            if (title != null ? !title.equals(that.title) : that.title != null) return false;
            if (values != null ? !values.equals(that.values) : that.values != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + (values != null ? values.hashCode() : 0);
            return result;
        }
    }
}
