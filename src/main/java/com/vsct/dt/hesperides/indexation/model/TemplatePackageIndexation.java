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
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by william_montaz on 05/12/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy"})
public final class TemplatePackageIndexation {

    private final String version;
    private final boolean workingCopy;
    private final String name;

    @JsonCreator
    public TemplatePackageIndexation(@JsonProperty("name") final String name,
                                     @JsonProperty("version") final String version,
                                     @JsonProperty("working_copy") final boolean workingCopy) {
        this.name = name;
        this.version = version;
        this.workingCopy = workingCopy;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplatePackageIndexation)) return false;

        TemplatePackageIndexation templatePackage = (TemplatePackageIndexation) o;

        if (workingCopy != templatePackage.workingCopy) return false;
        if (name != null ? !name.equals(templatePackage.name) : templatePackage.name != null) return false;
        if (version != null ? !version.equals(templatePackage.version) : templatePackage.version != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (workingCopy ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Techno{" +
                "version='" + version + '\'' +
                ", workingCopy=" + workingCopy +
                ", name='" + name + '\'' +
                '}';
    }

}
