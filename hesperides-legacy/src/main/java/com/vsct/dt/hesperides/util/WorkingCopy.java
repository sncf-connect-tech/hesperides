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
 * Created by william_montaz on 19/02/2015.
 */
public class WorkingCopy {
    /**
     * Workingcopy.
     */
    public static String UC = "WORKINGCOPY";

    /**
     * Workingcopy.
     */
    public static String LC = "workingcopy";

    /**
     * Workingcopy log text.
     */
    public static String TEXT = "WorkingCopy";

    /**
     * Workingcopy short text.
     */
    public static String SHORT = "wc";

    /**
     * Return true if text match with working copy.
     *
     * @param item text
     * @return
     */
    public static boolean is(final String item) {
        return UC.equals(item.toUpperCase());
    }

    public static HesperidesVersion of(final String versionName) {
        return new HesperidesVersion(versionName, true);
    }

    public static HesperidesVersion of(final HesperidesVersion version) {
        return new HesperidesVersion(version.getVersionName(), true);
    }
}
