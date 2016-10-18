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

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

/**
 * Created by emeric_martineau on 26/10/2015.
 */
public class PlatformData extends AbstractPlatformData {
    private PlatformData() {
        // nothing
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformData)) return false;

        PlatformData that = (PlatformData) o;

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

    public static IApplicationName withPlatformName(final String name) {
        return new Builder(name);
    }

    public static interface IApplicationName {
        IApplicationVersion withApplicationName(String name);
    }

    public static interface IApplicationVersion {
        IModules withApplicationVersion(String version);
    }

    public static interface IModules {
        IVersion withModules(Set<ApplicationModuleData> modules);
    }

    public static interface IVersion {
        IProduction withVersion(long version);
    }

    public static interface IProduction extends IBuilder {
        IBuilder isProduction();

        IBuilder setProduction(boolean production);
    }

    public static interface IBuilder {
        PlatformData build();
    }

    public static class Builder implements IApplicationName, IApplicationVersion, IModules, IProduction, IBuilder, IVersion {
        private PlatformData platform = new PlatformData();

        public Builder(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Platform name contain wrong character");
            platform.platformName = name;
        }

        @Override
        public IApplicationVersion withApplicationName(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Application name scontain wrong character");
            platform.applicationName = name;
            return this;
        }

        @Override
        public IModules withApplicationVersion(final String version) {
            checkArgument(!isNonDisplayedChar(version), "Application version contain wrong character");
            platform.applicationVersion = version;
            return this;
        }

        @Override
        public PlatformData build() {
            return platform;
        }

        @Override
        public IVersion withModules(final Set<ApplicationModuleData> modules) {
            checkNotNull(modules, "Modules should not be null");
            // TODO emeric : can be empty ?
            //checkArgument(!modules.isEmpty(), "Modules should not be empty");

            platform.modules = ImmutableSet.copyOf(modules);
            return this;
        }

        @Override
        public IBuilder isProduction() {
            platform.production = true;
            return this;
        }

        @Override
        public IProduction withVersion(final long version) {
            // Don't check version cause at creation version is -1
            //checkState(version > 0, "Version must be set and positive");
            platform.versionID = version;
            return this;
        }

        @Override
        public IBuilder setProduction(final boolean production) {
            platform.production = production;
            return this;
        }
    }
}
