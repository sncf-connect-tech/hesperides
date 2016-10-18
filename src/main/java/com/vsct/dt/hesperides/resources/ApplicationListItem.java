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
 * Class Representing a small description of an application.
 * It is used in list to be returned via REST services
 * Created by william_montaz on 30/12/2014.
 */
public final class ApplicationListItem {

    /**
     * Name of the application represented by this item.
     */
    private String name;

    /**
     * Default constructor used for Jackson, keep it private.
     */
    private ApplicationListItem() {
    }

    /**
     * Constructor to be used.
     *
     * @param name The application name
     */
    public ApplicationListItem(final String name) {
        this.name = name;
    }

    /**
     * Getter for name.
     *
     * @return the application name
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
        final ApplicationListItem other = (ApplicationListItem) obj;
        return Objects.equals(this.name, other.name);
    }
}
