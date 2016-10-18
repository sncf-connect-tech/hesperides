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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vsct.dt.hesperides.templating.Template;
import io.dropwizard.jackson.JsonSnakeCase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by william_montaz on 29/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"id", "name", "namespace", "filename", "location"})
public final class TemplateListItem {
    private String name;
    private String namespace;
    private String filename;
    private String location;

    private TemplateListItem() {
        //Jackson
    }

    public TemplateListItem(String namespace, String name, String filename, String location){
        checkNotNull(namespace);
        checkNotNull(name);
        checkNotNull(filename);
        checkNotNull(location);

        this.name = name;
        this.namespace = namespace;
        this.filename = filename;
        this.location = location;
    }

    public TemplateListItem(final Template template) {
        this(template.getNamespace(), template.getName(), template.getFilename(), template.getLocation());
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateListItem that = (TemplateListItem) o;

        if (!name.equals(that.name)) return false;
        if (!namespace.equals(that.namespace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + namespace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TemplateListItem{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
