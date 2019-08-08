/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.templatecontainers;

public class VersionTypes {

    public final static String WORKINGCOPY = "workingcopy";
    public final static String RELEASE = "release";

    public static Boolean toIsWorkingCopy(String versionType) {
        Boolean isWorkingCopy;
        if (WORKINGCOPY.equalsIgnoreCase(versionType)) {
            isWorkingCopy = true;
        } else if (RELEASE.equalsIgnoreCase(versionType)) {
            isWorkingCopy = false;
        } else {
            isWorkingCopy = null;
        }
        return isWorkingCopy;
    }

    public static String fromIsWorkingCopy(Boolean isWorkingCopy) {
        String versionType;
        if (isWorkingCopy == null) {
            versionType = "";
        } else if (isWorkingCopy) {
            versionType = WORKINGCOPY;
        } else {
            versionType = RELEASE;
        }
        return versionType;
    }
}
