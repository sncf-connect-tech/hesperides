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

package com.vsct.dt.hesperides.templating.models;

/**
 * Temporary value to manage protected string.
 *
 * Created by emeric_martineau on 05/11/2015.
 */
public class TemporaryValueProperty {
    /**
     * Value.
     */
    private String value;

    /**
     * Real length. Can be different in case of protected string.
     */
    private int length;

    public TemporaryValueProperty(final String value, final int len) {
        this.value = value;
        this.length = len;
    }

    public String getValue() {
        return value;
    }

    public int length() {
        return length;
    }
}
