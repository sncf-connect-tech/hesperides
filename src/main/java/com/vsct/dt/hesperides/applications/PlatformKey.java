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

package com.vsct.dt.hesperides.applications;

import java.util.Objects;

/**
 * Created by william_montaz on 27/02/2015.
 */
public class PlatformKey {

    private String name;
    private String applicationName;

    private PlatformKey() {
        // Nothing
    }

    public PlatformKey(final String entityName) {
        final String[] keys = entityName.split("-", 2);
        this.applicationName = keys[0];
        this.name = keys[1];
    }

    public PlatformKey(String applicationName, String platformName){
        this.name = platformName;
        this.applicationName = applicationName;
    }

    public String getName() {
        return name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public static IApplicationName withName(String name){
        return new Builder(name);
    }

    public String getEntityName() {
        String builder = applicationName +
                "-" +
                name;
        return builder;
    }

    @Override
    public String toString() {
        return "PlatformInfo{" +
                "name='" + name + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, applicationName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlatformKey other = (PlatformKey) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.applicationName, other.applicationName);
    }

    public static interface IApplicationName {
        public IBuild withApplicationName(String applicationName);
    }

    public static interface IBuild {
        public PlatformKey build();
    }

    private static class Builder implements IApplicationName, IBuild{

        private PlatformKey instance = new PlatformKey();

        public Builder(String name) {
            instance.name = name;
        }

        @Override
        public IBuild withApplicationName(String applicationName) {
            instance.applicationName = applicationName;
            return this;
        }

        @Override
        public PlatformKey build() {
            return instance;
        }

    }


}
