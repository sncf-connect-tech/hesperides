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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.*;
import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by emeric_martineau on 26/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy"})
public class ApplicationModuleData {
    private static final String VALORISATION_KEY_MODULE_NAME = "hesperides.module.name";
    private static final String VALORISATION_KEY_MODULE_VERSION = "hesperides.module.version";
    private static final String VALORISATION_KEY_MODULE_PATH = "hesperides.module.path";
    private static final String VALORISATION_KEY_MODULE_FULL_PATH = "hesperides.module.path.full";

    /**
     * This id is used to detect module updates when version or path is changed
     * First time a module is provided, no id is given, hesperides will give it one
     */
    @JsonProperty("id")
    private int id;
    private String name;

    @JsonProperty("version")
    private String version;

    @JsonProperty("working_copy")
    private boolean workingCopy;

    @JsonProperty("path")
    private String path;

    @JsonProperty("instances")
    @JsonDeserialize(as = Set.class)
    private Set<InstanceData> instances;

    private ApplicationModuleData() {
        //Nothing
    }

    @JsonCreator
    protected ApplicationModuleData(@JsonProperty("name") final String name,
                                    @JsonProperty("version") final String version,
                                    @JsonProperty("working_copy") final boolean isWorkingCopy,
                                    @JsonProperty("path") final String path,
                                    @JsonProperty("instances") final Set<InstanceData> instances,
                                    @JsonProperty("id") final int moduleId) {
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
        StringBuilder builder = new StringBuilder();
        //We ensure that path will always start with # even if it is omitted by the caller
        if (!path.startsWith("#")) {
            builder.append('#');
        }
        builder.append(path);
        this.path = builder.toString();
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

    public Set<InstanceData> getInstances() {
        return Sets.newHashSet(instances);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationModuleData)) return false;

        ApplicationModuleData applicationModule = (ApplicationModuleData) o;

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

    public String getPropertiesPath() {
        return this.getPath() + "#" + this.getName() + "#" + this.getVersion() + "#"
                + (this.isWorkingCopy() ? WorkingCopy.UC : Release.UC);
    }

    public Set<KeyValueValorisationData> generateHesperidesPredefinedScope() {
        Set<KeyValueValorisationData> predefinedScope = new HashSet<>();
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_MODULE_NAME, this.getName()));
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_MODULE_VERSION, this.getVersion()));
        predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_MODULE_FULL_PATH, this.getPath().replace('#', '/')));

        /* Construct path related valorisations
        Since path always start with #, using split produces an array starting with an empty string, thus we need to offset by 1
         */
        String[] path_tokens = this.getPath().split("#");
        for (int path_index = 1; path_index < path_tokens.length; path_index++) {
            predefinedScope.add(new KeyValueValorisationData(VALORISATION_KEY_MODULE_PATH + "." + (path_index - 1), path_tokens[path_index]));
        }

        return predefinedScope;
    }

    public Optional<InstanceData> getInstance(String instanceName, Boolean simulate_empty) {
        for (InstanceData instance : instances) {
            if (instance.getName().equals(instanceName)) {
                return Optional.of(instance);
            }
        }
        if (simulate_empty) {
            return Optional.of(InstanceData.withInstanceName(instanceName).withKeyValue(new HashSet<>()).build());
        }
        return Optional.empty();
    }


    public Optional<InstanceData> getInstance(String instanceName) {
        return getInstance(instanceName, false);
    }

    public static IVersion withApplicationName(final String name) {
        return new Builder(name);
    }

    public static interface IVersion {
        IPath withVersion(String version);
    }

    public static interface IPath {
        IId withPath(String path);
    }

    public static interface IId {
        IInstances withId(int id);
    }

    public static interface IInstances {
        IWorkingcopy withInstances(Set<InstanceData> instances);
    }

    public static interface IWorkingcopy extends IBuilder, IWorkingcopySet {
        IBuilder isWorkingcopy();
    }

    public static interface IWorkingcopySet extends IBuilder {
        IBuilder setWorkingcopy(boolean workingcopy);
    }

    public static interface IBuilder {
        ApplicationModuleData build();
    }

    public static class Builder implements IVersion, IPath, IInstances, IId, IWorkingcopy {
        private ApplicationModuleData applicationModuleData = new ApplicationModuleData();

        public Builder(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Instance name contain wrong character");

            applicationModuleData.name = name;
        }

        @Override
        public ApplicationModuleData build() {
            return applicationModuleData;
        }

        @Override
        public IWorkingcopy withInstances(final Set<InstanceData> instances) {
            checkNotNull(instances, "Instances should not be null");

            applicationModuleData.instances = ImmutableSet.copyOf(instances);

            return this;
        }

        @Override
        public IId withPath(final String path) {
            checkArgument(!isNonDisplayedChar(path), "Path should contain wrong character");

            if (path.startsWith("#")) {
                applicationModuleData.path = path;
            } else {
                applicationModuleData.path = "#".concat(path);
            }

            return this;
        }

        @Override
        public IPath withVersion(final String version) {
            checkArgument(!isNonDisplayedChar(version), "Path should contain wrong character");

            applicationModuleData.version = version;

            return this;
        }

        @Override
        public IBuilder isWorkingcopy() {
            applicationModuleData.workingCopy = true;
            return this;
        }

        @Override
        public IBuilder setWorkingcopy(final boolean workingcopy) {
            applicationModuleData.workingCopy = workingcopy;
            return this;
        }

        @Override
        public IInstances withId(final int id) {
            checkState(id >= 0, "Id must be set and positive");
            applicationModuleData.id = id;
            return this;
        }
    }
}
