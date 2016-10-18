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

import java.util.Set;

/**
 * Created by william_montaz on 11/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class IterablePropertyModel extends Property {
    private final Set<Property> fields;

    public IterablePropertyModel(final IterableCode code) {
        super(code);
        this.fields = Sets.newHashSet();
        for (final Code childCode : code.getCodes()) {
            if (childCode instanceof ValueCode) {
                this.addField(new KeyValuePropertyModel((ValueCode) childCode));
            }
            else if (childCode instanceof IterableCode) {
                this.addField(new IterablePropertyModel((IterableCode) childCode));
            }
        }
    }

    @JsonCreator
    public IterablePropertyModel(@JsonProperty("name") final String name,
                                 @JsonProperty("comment") final String comment,
                                 @JsonProperty("fields") final Set<Property> fields) {
        super(name, comment);
        this.fields = Sets.newHashSet(fields);
    }

    /* Should be used only in constructor to ensure immutability */
    private void addField(final Property field) {
        this.fields.add(field);
    }

    public Set<Property> getFields() {
        return Sets.newHashSet(fields);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IterablePropertyModel)) return false;
        if (!super.equals(o)) return false;

        IterablePropertyModel that = (IterablePropertyModel) o;

        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
