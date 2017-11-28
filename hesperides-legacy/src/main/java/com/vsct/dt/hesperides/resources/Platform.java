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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.vsct.dt.hesperides.applications.PlatformKey;
import io.dropwizard.jackson.JsonSnakeCase;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by william_montaz on 10/12/2014.
 */
@JsonSnakeCase
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"platform_name", "application_name", "application_version", "production", "platform_global_module", "modules", "version_id"})
public class Platform {

    @NotNull
    @NotEmpty
    @JsonProperty("platform_name")
    private final String platformName;

    @NotNull
    @NotEmpty
    @JsonProperty("application_name")
    private final String applicationName;

    @NotNull
    @NotEmpty
    @JsonProperty("application_version")
    private final String applicationVersion;

    @JsonProperty("modules")
    @JsonDeserialize(as = ImmutableSet.class)
    private final Set<ApplicationModule> modules;

    @JsonProperty("production")
    private final boolean production; //default value, some events have not this field

    @JsonProperty("version_id")
    private final long versionID; //initial default value

    @JsonCreator
    public Platform(@JsonProperty("platform_name") final String platformName,
                    @JsonProperty("application_name") final String applicationName,
                    @JsonProperty("application_version") final String applicationVersion,
                    @JsonProperty("production") final boolean isProduction,
                    @JsonProperty("modules") final Set<ApplicationModule> modules,
                    @JsonProperty("version_id") final long versionID) {
        this.versionID = versionID;
        this.platformName = platformName;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.modules = ImmutableSet.copyOf(modules);
        this.production = isProduction;
    }

    public Platform(final PlatformKey key, final String applicationVersion, final boolean isProduction, final Set<ApplicationModule> modules, final long versionID) {
        this(key.getName(), key.getApplicationName(), applicationVersion, isProduction, modules, versionID);
    }

    public Platform(PlatformKey key, String applicationVersion, boolean isProduction, Set<ApplicationModule> modules) {
        this(key.getName(), key.getApplicationName(), applicationVersion, isProduction, modules, 1L);
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

    public Set<ApplicationModule> getModules() {
        return modules;
    }

    public boolean isProduction() {
        return production;
    }

    public long getVersionID() {
        return versionID;
    }

    @Override
    public String toString() {
        return "PlatformVO{" +
                "platformName='" + platformName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Platform)) return false;

        Platform that = (Platform) o;

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

    @JsonIgnore
    public PlatformKey getKey() {
        return PlatformKey.withName(platformName).withApplicationName(applicationName).build();
    }
}
