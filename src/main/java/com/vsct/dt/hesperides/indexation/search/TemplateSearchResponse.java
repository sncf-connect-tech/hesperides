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

package com.vsct.dt.hesperides.indexation.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by william_montaz on 29/10/2014.
 */
public final class TemplateSearchResponse {

    private String namespace;
    private String name;

    @JsonCreator
    public TemplateSearchResponse(@JsonProperty("hesnamespace") final String namespace,
                                  @JsonProperty("name") final String name) {
        this.namespace = namespace;
        this.name = name;
    }

    @JsonProperty(value = "hesnamespace")
    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TemplateSearchResponse other = (TemplateSearchResponse) obj;
        return Objects.equals(this.namespace, other.namespace)
                && Objects.equals(this.name, other.name);
    }
}
