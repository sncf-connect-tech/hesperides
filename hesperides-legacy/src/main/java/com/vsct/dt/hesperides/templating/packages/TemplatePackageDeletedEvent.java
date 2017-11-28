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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by william_montaz on 16/04/2015.
 */
public class TemplatePackageDeletedEvent {

    private final String packageName;
    private final String packageVersion;
    private final boolean workingCopy;

    @JsonCreator
    public TemplatePackageDeletedEvent(@JsonProperty("packageName") String packageName,
                                       @JsonProperty("packageVersion") String packageVersion,
                                       @JsonProperty("workingCopy") boolean isWorkingCopy) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.workingCopy = isWorkingCopy;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplatePackageDeletedEvent that = (TemplatePackageDeletedEvent) o;

        if (workingCopy != that.workingCopy) return false;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
        if (packageVersion != null ? !packageVersion.equals(that.packageVersion) : that.packageVersion != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (packageVersion != null ? packageVersion.hashCode() : 0);
        result = 31 * result + (workingCopy ? 1 : 0);
        return result;
    }
}
