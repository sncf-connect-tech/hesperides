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

package com.vsct.dt.hesperides.util;

/**
 * Created by william_montaz on 23/01/2015.
 */
public class Version {
    private final String version;
    private final boolean workingCopy;

    public Version(String version, boolean workingCopy) {
        this.version = version;
        this.workingCopy = workingCopy;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public boolean isGreaterThan(Version other) {
        //Compare version numbers by tokenizing
        String[] thisVersionNumbers = this.version.split("\\.");
        String[] otherVersionNumbers = other.getVersion().split("\\.");

        int maxLen = thisVersionNumbers.length;
        if (otherVersionNumbers.length > maxLen) maxLen = otherVersionNumbers.length;

        for (int i = 0; i < maxLen; i++) {
            int thisDigit = (i < thisVersionNumbers.length) ? Integer.parseInt(thisVersionNumbers[i]) : 0;
            int otherDigit = (i < otherVersionNumbers.length) ? Integer.parseInt(otherVersionNumbers[i]) : 0;
            if (thisDigit > otherDigit) {
                return true;
            }
            if (thisDigit < otherDigit) {
                return false;
            }
        }

        //reaching this point version numbers are equals so let's compare realese vs working copy
        return !this.isWorkingCopy() && other.isWorkingCopy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version version1 = (Version) o;

        if (workingCopy != version1.workingCopy) return false;
        if (version != null ? !version.equals(version1.version) : version1.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (workingCopy ? 1 : 0);
        return result;
    }
}
