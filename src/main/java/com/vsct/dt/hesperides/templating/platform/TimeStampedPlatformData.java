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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by william_montaz on 04/03/2015.
 */
public class TimeStampedPlatformData extends AbstractPlatformData {
    private long timestamp;

    private TimeStampedPlatformData() {
        // Nothing
    }

    @JsonCreator
    protected TimeStampedPlatformData(@JsonProperty("platform") PlatformData platform,
                               @JsonProperty("timestamp") long timestamp) {
        super(platform.getKey().getName(), platform.getKey().getApplicationName(), platform.getApplicationVersion(),
                platform.isProduction(), platform.getModules(), platform.getVersionID());
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeStampedPlatformData that = (TimeStampedPlatformData) o;

        if (timestamp != that.timestamp) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    public static IApplicationName withPlatformName(final String name) {
        return new Builder(name);
    }

    public static ITimestamp withPlatform(final PlatformData platform) {
        return new Builder(platform.getPlatformName())
                .withApplicationName(platform.getApplicationName())
                .withApplicationVersion(platform.getApplicationVersion())
                .withModules(platform.getModules())
                .withVersion(platform.getVersionID())
                .setProduction(platform.isProduction());
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

    public static interface IProduction extends ITimestamp {
        ITimestamp isProduction();

        ITimestamp setProduction(boolean production);
    }

    public static interface ITimestamp extends IBuilder {
        IBuilder withTimestamp(long timestamp);
    }

    public static interface IBuilder {
        TimeStampedPlatformData build();
    }

    public static class Builder implements IApplicationName, IApplicationVersion, IModules, IProduction,
            IBuilder, IVersion, ITimestamp {
        private TimeStampedPlatformData platform = new TimeStampedPlatformData();

        public Builder(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Platform name contain wrong character");
            platform.platformName = name;
        }

        @Override
        public IApplicationVersion withApplicationName(final String name) {
            checkArgument(!isNonDisplayedChar(name), "Application name contain wrong character");
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
        public TimeStampedPlatformData build() {
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
        public ITimestamp isProduction() {
            platform.production = true;
            return this;
        }

        @Override
        public IProduction withVersion(final long version) {
            checkState(version > 0, "Version must be set and positive");
            platform.versionID = version;
            return this;
        }

        @Override
        public IBuilder withTimestamp(final long timestamp) {
            checkState(timestamp > 0, "Timestamp must be set and positive");
            platform.timestamp = timestamp;
            return this;
        }

        @Override
        public ITimestamp setProduction(final boolean production) {
            platform.production = production;
            return this;
        }
    }
}
