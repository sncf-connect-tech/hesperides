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

package com.vsct.dt.hesperides.util.converter;

import com.vsct.dt.hesperides.resources.Application;
import com.vsct.dt.hesperides.templating.platform.ApplicationData;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public interface ApplicationConverter {
    /**
     * Convert Application to ApplicationData.
     *
     * @param appli input
     * @return output
     */
    ApplicationData toApplicationData(Application appli);

    /**
     * Convert ApplicationData to Application.
     *
     * @param appli input
     * @return output
     */
    Application toApplication(ApplicationData appli);
}
