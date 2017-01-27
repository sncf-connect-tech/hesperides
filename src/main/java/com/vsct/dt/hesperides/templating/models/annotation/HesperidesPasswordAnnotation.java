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

import org.apache.commons.lang.StringUtils;

/**
 * Created by emeric_martineau on 05/11/2015.
 */
public class HesperidesPasswordAnnotation extends AbstractHesperidesAnnotation {
    /**
     * Constructor.
     *
     * @param name  name of annotation
     * @param value value of annotation
     */
    public HesperidesPasswordAnnotation(final String name, final String value) {
        super(name, value);
    }

    @Override
    public boolean isValid() {
        final String value = getValue();

        return (StringUtils.isEmpty(value) || StringUtils.isBlank(value));
    }
}
