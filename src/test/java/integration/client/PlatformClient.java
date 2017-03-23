/*
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
 */

package integration.client;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;

import io.dropwizard.jackson.JsonSnakeCase;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.ApplicationModule;

/**
 * Created by william_montaz on 10/12/2014.
 */
@JsonSnakeCase
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"platform_name", "application_name", "application_version", "production", "platform_global_module", "modules", "version_id"})
public class PlatformClient {

    @JsonProperty("platform_name")
    private final String                          platformName;

    @JsonProperty("application_name")
    private final String                          applicationName;

    @JsonProperty("application_version")
    private final String                          applicationVersion;

    @JsonProperty("modules")
    @JsonDeserialize(as = HashSet.class)
    private final Set<ApplicationModule> modules;

    @JsonProperty("production")
    private final boolean                         production; //default value, some events have not this field

    @JsonProperty("version_id")
    private final long                            versionID; //initial default value

    @JsonCreator
    public PlatformClient(@JsonProperty("platform_name") final String platformName,
                    @JsonProperty("application_name") final String applicationName,
                    @JsonProperty("application_version") final String applicationVersion,
                    @JsonProperty("production") final boolean isProduction,
                    @JsonProperty("modules") final Set<ApplicationModule> modules,
                    @JsonProperty("version_id") final long versionID) {
        this.versionID = versionID;
        this.platformName = platformName;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.modules = modules;
        this.production = isProduction;
    }

    public PlatformClient(final PlatformKey key, final String applicationVersion, final boolean isProduction, final Set<ApplicationModule> modules,
            final long versionID){
        this(key.getName(), key.getApplicationName(), applicationVersion, isProduction, modules, versionID);
    }

    public PlatformClient(PlatformKey key, String applicationVersion, boolean isProduction, Set<ApplicationModule> modules) {
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
        if (!(o instanceof PlatformClient)) return false;

        PlatformClient that = (PlatformClient) o;

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
