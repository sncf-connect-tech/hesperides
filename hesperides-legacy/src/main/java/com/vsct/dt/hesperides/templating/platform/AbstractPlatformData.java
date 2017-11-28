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

package com.vsct.dt.hesperides.templating.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableSet;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.storage.DomainVersionable;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 26/10/2015.
 */
@JsonSnakeCase
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"platform_name", "application_name", "application_version", "production", "platform_global_module", "modules", "version_id"})
public abstract class AbstractPlatformData extends DomainVersionable {
    private static final String VALORISATION_KEY_APP_NAME = "hesperides.application.name";
    private static final String VALORISATION_KEY_APP_VERSION = "hesperides.application.version";
    private static final String VALORISATION_KEY_PLTFM_NAME = "hesperides.platform.name";

    protected String platformName;
    protected String applicationName;
    protected String applicationVersion;
    protected ImmutableSet<ApplicationModuleData> modules;
    protected boolean production; //default value, some events have not this field

    protected AbstractPlatformData() {
        // nothing
    }

    @JsonCreator
    protected AbstractPlatformData(
            @JsonProperty("platform_name") final String platformName,
            @JsonProperty("application_name") final String applicationName,
            @JsonProperty("application_version") final String applicationVersion,
            @JsonProperty("production") final boolean isProduction,
            @JsonProperty("modules") final Set<ApplicationModuleData> modules,
            @JsonProperty("version_id") final long versionID) {
        this.versionID = versionID;
        this.platformName = platformName;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.modules = ImmutableSet.copyOf(modules);
        this.production = isProduction;
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

    public ImmutableSet<ApplicationModuleData> getModules() {
        return modules;
    }

    public boolean isProduction() {
        return production;
    }

    public PlatformKey getKey() {
        return PlatformKey.withName(platformName).withApplicationName(applicationName).build();
    }

    @Override
    public String toString() {
        return "PlatformVO{" +
                "platformName='" + platformName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }

    public Optional<ApplicationModuleData> findModule(String moduleName, String moduleVersion, boolean isModuleWorkingCopy, String path) {
        for (ApplicationModuleData module : modules) {
            if (module.getName().equals(moduleName)
                    && module.getVersion().equals(moduleVersion)
                    && module.isWorkingCopy() == isModuleWorkingCopy
                    && module.getPath().equals(path)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }

    public Set<KeyValueValorisationData> generateHesperidesPredefinedScope() {
        Set<KeyValueValorisationData> predefinedScope = new HashSet<>();
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_APP_NAME, this.applicationName));
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_APP_VERSION, this.applicationVersion));
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_PLTFM_NAME, this.platformName));
        return predefinedScope;
    }
}
