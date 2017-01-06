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

package com.vsct.dt.hesperides.templating.packages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.vsct.dt.hesperides.util.HesperidesVersion;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by william_montaz on 19/02/2015.
 */

public class TemplatePackageKey {

    @JsonProperty(value = "name")
    protected String name;
    
    //Tweak to guarantee old API
    @JsonProperty(value = "version")
    protected String versionName;

    //Tweak to guarantee old API
    @JsonProperty(value = "working_copy")
    protected boolean workingCopy;
    
    protected TemplatePackageKey(){}

    public TemplatePackageKey(final String name, final String version, final boolean isWorkingCopy) {
        this(name, new HesperidesVersion(version, isWorkingCopy));
    }

    public TemplatePackageKey(final String name, HesperidesVersion version){
        this.name = name;
        this.versionName = version.getVersionName();
        this.workingCopy = version.isWorkingCopy();
    }

    @JsonProperty(value = "name")
    public String getName() {
        return name;
    }

    @JsonProperty(value = "working_copy")
    public boolean isWorkingCopy() {
        return workingCopy;
    }

    @JsonProperty(value = "version")
    public String getVersionName() {
        return versionName;
    }

    @JsonIgnore
    public HesperidesVersion getVersion() {
        return new HesperidesVersion(versionName, workingCopy);
    }

    @JsonIgnore
    public String getNamespace() {
        return "packages#" + name + "#" + versionName + "#" + (workingCopy ? "WORKINGCOPY" : "RELEASE");
    }

    @JsonIgnore
    public String getEntityName() {
        return name + "-" + versionName + "-" + (workingCopy ? "wc" : "release");
    }

    @Override
    public String toString() {
        return "TemplatePackageKey{" +
                "name='" + name + '\'' +
                ", versionName='" + versionName + '\'' +
                ", workingCopy=" + workingCopy +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, versionName, workingCopy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof TemplatePackageKey)) return false;

        TemplatePackageKey that = (TemplatePackageKey) o;

        if (workingCopy != that.workingCopy) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (versionName != null ? !versionName.equals(that.versionName) : that.versionName != null) return false;

        return true;
    }

    /**
     * Builder method
     * @param name
     */
    public static IVersion withName(String name) {
        return new Builder(name);
    }

    public static interface IVersion {
        public IBuild withVersion(HesperidesVersion version);
    }

    public static interface IBuild {
        public TemplatePackageKey build();
    }

    private static class Builder implements IVersion, IBuild {

        private TemplatePackageKey instance = new TemplatePackageKey();

        public Builder(String name) {
            checkArgument(!Strings.isNullOrEmpty(name), "TemplatePackage name should not be empty or null");
            instance.name = name;
        }

        @Override
        public IBuild withVersion(HesperidesVersion version) {
            checkNotNull(version, "HesperidesVersion should not be null");
            instance.versionName = version.getVersionName();
            instance.workingCopy = version.isWorkingCopy();
            return this;
        }

        @Override
        public TemplatePackageKey build() {
            return instance;
        }

    }

}
