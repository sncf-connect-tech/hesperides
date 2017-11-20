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

import com.vsct.dt.hesperides.resources.Platform;
import com.vsct.dt.hesperides.templating.platform.AbstractPlatformData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public interface PlatformConverter {
    /**
     * Return instance converter.
     *
     * @return instance converter
     */
    InstanceConverter getInstanceConverter();

    /**
     * Convert Platform to PlatformData.
     *
     * @param platform input
     * @return output
     */
    PlatformData toPlatformData(Platform platform);

    /**
     * Convert Platform to PlatformData.
     *
     * @param platformData input
     * @return output
     */
    Platform toPlatform(AbstractPlatformData platformData);
}
