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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by william_montaz on 11/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "comment", "model", "valorisations"})
public final class IterablePropertyIndexation extends PropertyIndexation {
    private final Set<PropertyIndexation> model;
    private final List<Valorisation>              valorisations;

    public IterablePropertyIndexation(final String name, final String comment) {
        this(name, comment, Sets.newHashSet(), Lists.newArrayList());
    }

    @JsonCreator
    public IterablePropertyIndexation(@JsonProperty("name") final String name,
                                      @JsonProperty("comment") final String comment,
                                      @JsonProperty("model") final Set<PropertyIndexation> model,
                                      @JsonProperty("valorisations") final List<Valorisation> valorisations) {
        super(name, comment);
        this.model = Sets.newHashSet(model);
        this.valorisations = Lists.newArrayList(valorisations);
    }

    public Set<PropertyIndexation> getModel() {
        return Sets.newHashSet(model);
    }

    public List<Valorisation> getValorisations() {
        return Lists.newArrayList(valorisations);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(model, valorisations);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final IterablePropertyIndexation other = (IterablePropertyIndexation) obj;
        return Objects.equals(this.model, other.model)
                && Objects.equals(this.valorisations, other.valorisations);
    }

    @JsonPropertyOrder({"title", "model"})
    @JsonSnakeCase
    public static final class Valorisation {
        private final String title;
        private final Set<KeyValuePropertyIndexation> values;

        @JsonCreator
        public Valorisation(@JsonProperty("title") final String title,
                            @JsonProperty("values") final Set<KeyValuePropertyIndexation> values) {
            this.title = title;
            this.values = Sets.newHashSet(values);
        }

        public String getTitle() {
            return title;
        }

        public Set<KeyValuePropertyIndexation> getValues() {
            return Sets.newHashSet(values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, values);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Valorisation other = (Valorisation) obj;
            return Objects.equals(this.title, other.title)
                    && Objects.equals(this.values, other.values);
        }
    }
}
