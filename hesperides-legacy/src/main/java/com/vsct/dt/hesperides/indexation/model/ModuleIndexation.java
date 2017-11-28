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
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Objects;

/**
 * Created by william_montaz on 02/12/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy", "technos"})
public final class ModuleIndexation extends Data {

    private final String name;
    private final String version;
    private final boolean workingCopy;
    private final List<TemplatePackageIndexation> technos;

    @JsonCreator
    public ModuleIndexation(@JsonProperty("name") final String name,
                            @JsonProperty("version") final String version,
                            @JsonProperty("workingCopy") final boolean workingCopy,
                            @JsonProperty("technos") final List<TemplatePackageIndexation> technos) {
        this.name = name;
        this.version = version;
        this.workingCopy = workingCopy;
        this.technos = Lists.newArrayList(technos);
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

    public List<TemplatePackageIndexation> getTechnos() {
        return Lists.newArrayList(technos);
    }

    @Override
    public String toString() {
        return "HesperidesModule{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", workingCopy=" + workingCopy +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleIndexation)) return false;

        ModuleIndexation that = (ModuleIndexation) o;

        if (workingCopy != that.workingCopy) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (workingCopy ? 1 : 0);
        return result;
    }

    @Override
    protected int getKey() {
        return Objects.hash(name, version, workingCopy);
    }

    public String getNamespace() {
        return ModuleIndexation.getNamespace(this.name, this.version, this.workingCopy);
    }

    public static String getNamespace(String moduleName, String moduleVersion, boolean isWorkingCopy) {
        return "modules#" + moduleName + "#" + moduleVersion + "#" + (isWorkingCopy ? WorkingCopy.UC : Release.TEXT);
    }
}
