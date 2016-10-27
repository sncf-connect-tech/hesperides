/*
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
 */

package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformDeletedEvent;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformDeletedCommand implements HesperidesCommand<PlatformDeletedEvent> {
    private final PlatformKey key;
    private final PlatformRegistryInterface platformRegistry;
    private final PropertiesRegistryInterface propertiesRegistry;

    public PlatformDeletedCommand(final PlatformRegistryInterface platformRegistry,
                                  final PropertiesRegistryInterface propertiesRegistry, final PlatformKey key) {
        this.key = key;
        this.propertiesRegistry = propertiesRegistry;
        this.platformRegistry = platformRegistry;
    }

    @Override
    public PlatformDeletedEvent apply() {
        final String platformName = key.getName();
        final String applicationName = key.getApplicationName();

        final Optional<PlatformData> platformOptional = platformRegistry.getPlatform(key);

        if (platformOptional.isPresent()) {
            return new PlatformDeletedEvent(applicationName, platformName);
        } else {
            throw new MissingResourceException(key + " does not exists");
        }
    }

    @Override
    public void complete() {
        //remove platform
        platformRegistry.deletePlatform(key);
    }
}
