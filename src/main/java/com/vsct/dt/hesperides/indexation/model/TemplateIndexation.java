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
import io.dropwizard.jackson.JsonSnakeCase;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"id", "name", "hesnamespace", "filename", "location", "content"})
public final class TemplateIndexation extends Data {
    private final String name;
    private final String namespace;
    private final String filename;
    private final String location;
    private final String content;

    @JsonCreator
    public TemplateIndexation(@JsonProperty("hesnamespace") final String namespace,
                              @JsonProperty("name") final String name,
                              @JsonProperty("filename") final String filename,
                              @JsonProperty("location") final String location,
                              @JsonProperty("content") final String content) {
        this.namespace = namespace;
        this.name = name;
        this.location = location;
        this.filename = filename;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    @JsonProperty(value = "hesnamespace")
    public String getNamespace() {
        return namespace;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace, filename, location, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TemplateIndexation other = (TemplateIndexation) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.namespace, other.namespace)
                && Objects.equals(this.filename, other.filename)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.content, other.content);
    }

    @Override
    public String toString() {
        return "Template{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    protected int getKey() {
        return Objects.hash(namespace);
    }
}
