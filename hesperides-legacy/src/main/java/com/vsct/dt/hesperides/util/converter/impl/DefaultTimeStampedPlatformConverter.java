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

package com.vsct.dt.hesperides.util.converter.impl;

import com.vsct.dt.hesperides.resources.TimeStampedPlatform;
import com.vsct.dt.hesperides.templating.platform.TimeStampedPlatformData;
import com.vsct.dt.hesperides.util.converter.PlatformConverter;
import com.vsct.dt.hesperides.util.converter.TimeStampedPlatformConverter;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public class DefaultTimeStampedPlatformConverter implements TimeStampedPlatformConverter {
    final PlatformConverter platformConverter;

    public DefaultTimeStampedPlatformConverter() {
        this.platformConverter = new DefaultPlatformConverter();
    }

    public DefaultTimeStampedPlatformConverter(final PlatformConverter platformConverter) {
        this.platformConverter = platformConverter;
    }

    @Override
    public TimeStampedPlatform toTimeStampedPlatform(final TimeStampedPlatformData timeStampedPlatformData) {
        return new TimeStampedPlatform(platformConverter.toPlatform(timeStampedPlatformData),
                timeStampedPlatformData.getTimestamp());
    }

    @Override
    public TimeStampedPlatformData toTimeStampedPlatformData(final TimeStampedPlatform timeStampedPlatform) {
        return TimeStampedPlatformData
                .withPlatform(platformConverter.toPlatformData(timeStampedPlatform))
                .withTimestamp(timeStampedPlatform.getTimestamp()).build();
    }

    @Override
    public PlatformConverter getPlatformConverter() {
        return platformConverter;
    }
}
