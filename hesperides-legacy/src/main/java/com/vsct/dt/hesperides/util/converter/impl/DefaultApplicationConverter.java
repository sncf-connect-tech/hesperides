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

import com.vsct.dt.hesperides.resources.Application;
import com.vsct.dt.hesperides.resources.Platform;
import com.vsct.dt.hesperides.templating.platform.ApplicationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.util.converter.ApplicationConverter;
import com.vsct.dt.hesperides.util.converter.PlatformConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public class DefaultApplicationConverter implements ApplicationConverter {
    final PlatformConverter platformConverter;

    public DefaultApplicationConverter() {
        this.platformConverter = new DefaultPlatformConverter();
    }

    public DefaultApplicationConverter(PlatformConverter platformConverter) {
        this.platformConverter = platformConverter;
    }

    @Override
    public ApplicationData toApplicationData(final Application appli) {
        return new ApplicationData(appli.getName(), toListPlatformData(appli.getPlatforms()));

    }

    @Override
    public Application toApplication(final ApplicationData appli) {
        return new Application(appli.getName(), toListPlatform(appli.getPlatforms()));
    }

    /**
     * Convert list of Platform to list of PlatformData
     * @param listPlatform input
     * @return output
     */
    private Set<PlatformData> toListPlatformData(final Collection<Platform> listPlatform) {
        Set<PlatformData> newSetPlatformData = new HashSet<>(listPlatform.size());

        for (Platform platform : listPlatform) {
            newSetPlatformData.add(
                    platformConverter.toPlatformData(platform)
            );
        }

        return newSetPlatformData;
    }

    /**
     * Convert list of PlatformData to list of Platform
     * @param listPlatform input
     * @return output
     */
    private Set<Platform> toListPlatform(final List<PlatformData> listPlatform) {
        Set<Platform> newSetPlatformData = new HashSet<>(listPlatform.size());

        for (PlatformData platform : listPlatform) {
            newSetPlatformData.add(
                    platformConverter.toPlatform(platform)
            );
        }

        return newSetPlatformData;
    }
}
