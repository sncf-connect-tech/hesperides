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
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Set;

/**
 * Created by william_montaz on 10/12/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy", "properties_path", "path", "instances"})
public final class ApplicationModule {
    /**
     * This id is used to detect module updates when version or path is changed
     * First time a module is provided, no id is given, hesperides will give it one
     */
    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String        name;

    @JsonProperty("version")
    private final String        version;

    @JsonProperty("working_copy")
    private final boolean       workingCopy;

    @JsonProperty("path")
    private final String        path;

    @JsonProperty("instances")
    @JsonDeserialize(as = Set.class)
    private final Set<Instance> instances;

    @JsonCreator
    public ApplicationModule(@JsonProperty("name") final String name,
                             @JsonProperty("version") final String version,
                             @JsonProperty("working_copy") final boolean isWorkingCopy,
                             @JsonProperty("path") final String path,
                             @JsonProperty("instances") final Set<Instance> instances,
                             @JsonProperty("id") final int moduleId) {
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
        this.path = path;
        this.instances = Sets.newHashSet(instances);
        this.id = moduleId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public String getPath() {
        return path;
    }

    public Set<Instance> getInstances() {
        return Sets.newHashSet(instances);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationModule)) return false;

        ApplicationModule applicationModule = (ApplicationModule) o;

        if (workingCopy != applicationModule.workingCopy) return false;
        if (!name.equals(applicationModule.name)) return false;
        if (!path.equals(applicationModule.path)) return false;
        if (!version.equals(applicationModule.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (workingCopy ? 1 : 0);
        result = 31 * result + path.hashCode();
        return result;
    }

    @JsonProperty("properties_path")
    public String getPropertiesPath() {
        return this.getPath() + "#" + this.getName() + "#" + this.getVersion() + "#" + (this.isWorkingCopy() ? "WORKINGCOPY" : "RELEASE");
    }
}
