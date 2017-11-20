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

package com.vsct.dt.hesperides.templating.modules.template;

import com.fasterxml.jackson.annotation.*;
import com.vsct.dt.hesperides.storage.DomainVersionable;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"id", "name", "namespace", "filename", "location", "content", "rights", "version_id"})
public class Template extends DomainVersionable {
    private final String namespace;
    private final String name;
    private final String filename;
    private final String location;
    private final String content;
    private final TemplateRights rights;

    @JsonCreator
    public Template(@JsonProperty("namespace") final String namespace,
                    @JsonProperty("name") final String name,
                    @JsonProperty("filename") final String filename,
                    @JsonProperty("location") final String location,
                    @JsonProperty("content") final String content,
                    @JsonProperty("rights") final TemplateRights rights,
                    @JsonProperty("version_id") final long versionID) {
        super(versionID);
        this.namespace = namespace;
        this.name = name;
        this.location = location;
        this.filename = filename;
        this.content = content;
        this.rights = rights;
    }

    /*
    PUBLIC API
     */

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    public String getNamespace() {
        return namespace;
    }

    public TemplateRights getRights() {
        return rights;
    }

    //TODO move somewhere else
    public HesperidesPropertiesModel generatePropertiesModel() {
        TemplateSlurper templateSlurper = new TemplateSlurper(this.getContent());
        TemplateSlurper filenameSlurper = new TemplateSlurper(this.getFilename());
        TemplateSlurper locationSlurper = new TemplateSlurper(this.getLocation());
        HesperidesPropertiesModel templateProperties = templateSlurper.generatePropertiesScope();
        HesperidesPropertiesModel filenameProperties = filenameSlurper.generatePropertiesScope();
        HesperidesPropertiesModel locationProperties = locationSlurper.generatePropertiesScope();
        return templateProperties.merge(filenameProperties).merge(locationProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, filename, location, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Template other = (Template) obj;
        return Objects.equals(this.namespace, other.namespace)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.filename, other.filename)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.content, other.content)
                && Objects.equals(this.versionID, other.versionID);
    }

    public static void validateContent(final String content) {
        TemplateSlurper templateSlurper = new TemplateSlurper(content);
        /* This is a way to validate the template */
        templateSlurper.generatePropertiesScope();
    }

}
