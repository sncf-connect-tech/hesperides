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

package com.vsct.dt.hesperides.templating.models.annotation;

/**
 * Abstract hesperide annotation for property.
 *
 * Created by emeric_martineau on 05/11/2015.
 */
public abstract class AbstractHesperidesAnnotation implements HesperidesAnnotation {
    /**
     * Value of annotation (parameter).
     */
    private final String value;

    /**
     * Name of annotation.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param name name of annotation
     * @param value value of annotation
     */
    public AbstractHesperidesAnnotation(final String name, final String value) {
        this.value = value;
        this.name = name;
    }

    /**
     * Getter of value.
     *
     * @return value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Getter of name.
     *
     * @return name
     */
    @Override
    public String getName() {
        return name;
    }
}
