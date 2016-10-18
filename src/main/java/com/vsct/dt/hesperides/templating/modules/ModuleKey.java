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

package com.vsct.dt.hesperides.templating.modules;

import com.google.common.base.Strings;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.util.HesperidesVersion;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by william_montaz on 03/03/2015.
 */
public class ModuleKey extends TemplatePackageKey {

    private ModuleKey(){}

    public ModuleKey(String name, HesperidesVersion version) {
        super(name, version);
    }

    @Override
    public String getNamespace() {
        return "modules#" + name + "#" + versionName + "#" + (workingCopy ? "WORKINGCOPY" : "RELEASE");
    }

    @Override
    public String getEntityName() {
        return name + "-" + versionName + "-" + (workingCopy ? "wc" : "release");
    }

    @Override
    public String toString() {
        return "ModuleKey{" +
                "name='" + name + '\'' +
                ", versionName='" + versionName + '\'' +
                ", workingCopy=" + workingCopy +
                '}';
    }

    /**
     * Builder method
     * @param name
     */
    public static IVersion withModuleName(String name) {
        return new Builder(name);
    }

    public static interface IVersion {
        public IBuild withVersion(HesperidesVersion version);
    }

    public static interface IBuild {
        public ModuleKey build();
    }

    private static class Builder implements IVersion, IBuild {

        private ModuleKey instance = new ModuleKey();

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
        public ModuleKey build() {
            return instance;
        }

    }

}
