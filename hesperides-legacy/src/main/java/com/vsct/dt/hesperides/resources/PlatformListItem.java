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

package com.vsct.dt.hesperides.resources;

import java.util.Objects;

/**
 * Class Representing a small description of a platform.
 * It is used in list to be returned via REST services
 * Created by nicolas_boury on 15/10/2015.
 */
public final class PlatformListItem {

    /**
     * Name of the application represented by this item.
     */
    private String name;

    /**
     * Default constructor used for Jackson, keep it private.
     */
    private PlatformListItem() {
    }

    /**
     * Constructor to be used.
     *
     * @param name The platform name
     */
    public PlatformListItem(final String name) {
        this.name = name;
    }

    /**
     * Getter for name.
     *
     * @return the platform name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlatformListItem other = (PlatformListItem) obj;
        return Objects.equals(this.name, other.name);
    }
}
