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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Objects;

/**
 * Created by william_montaz on 10/12/2014.
 */
@JsonSnakeCase
@JsonPropertyOrder({"platform_name", "application_name", "application_version", "modules"})
public final class PlatformIndexation extends Data {

    private final String platformName;
    private final String applicationName;
    private final String applicationVersion;
    private final ImmutableList<PlatformModuleIndexation> modules;

    @JsonCreator
    public PlatformIndexation(@JsonProperty("platform_name") final String platformName,
                              @JsonProperty("application_name") final String applicationName,
                              @JsonProperty("application_version") final String applicationVersion,
                              @JsonProperty("modules") final List<PlatformModuleIndexation> modules) {
        this.platformName = platformName;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.modules = ImmutableList.copyOf(modules);
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public ImmutableList<PlatformModuleIndexation> getModules() {
        return modules;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformIndexation)) return false;

        PlatformIndexation that = (PlatformIndexation) o;

        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
            return false;
        if (platformName != null ? !platformName.equals(that.platformName) : that.platformName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = platformName != null ? platformName.hashCode() : 0;
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        return result;
    }

    @Override
    protected int getKey() {
        return Objects.hash(applicationName, platformName);
    }

    @JsonSnakeCase
    @JsonPropertyOrder({"name", "version", "working_copy", "path"})
    public static final class PlatformModuleIndexation {

        private final String name;
        private final String version;
        private final boolean workingCopy;
        private final String path;

        @JsonCreator
        public PlatformModuleIndexation(@JsonProperty("name") final String name,
                                        @JsonProperty("version") final String version,
                                        @JsonProperty("working_copy") final boolean isWorkingCopy,
                                        @JsonProperty("path") final String path) {
            this.name = name;
            this.version = version;
            this.workingCopy = isWorkingCopy;
            this.path = path;
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

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof PlatformModuleIndexation)) return false;

            PlatformModuleIndexation moduleVO = (PlatformModuleIndexation) o;

            if (workingCopy != moduleVO.workingCopy) return false;
            if (!name.equals(moduleVO.name)) return false;
            if (!path.equals(moduleVO.path)) return false;
            if (!version.equals(moduleVO.version)) return false;

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
    }
}
