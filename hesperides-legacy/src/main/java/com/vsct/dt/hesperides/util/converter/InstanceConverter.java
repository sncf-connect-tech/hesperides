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

import com.vsct.dt.hesperides.resources.Instance;
import com.vsct.dt.hesperides.templating.platform.InstanceData;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public interface InstanceConverter {
    /**
     * Convert Instance to InstanceData.
     *
     * @param instance input
     * @return output
     */
    InstanceData toInstanceData(Instance instance);

    /**
     * Convert InstanceData to Instance.
     *
     * @param instance input
     * @return output
     */
    Instance toInstance(InstanceData instance);
}
