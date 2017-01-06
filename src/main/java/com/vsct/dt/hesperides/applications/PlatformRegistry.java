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

package com.vsct.dt.hesperides.applications;

import com.google.common.collect.Maps;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * This platform registry holds everything inmemory.
 * It might be updated for further development in order to manage memory in a better and safier way
 */
class PlatformRegistry {

    private ConcurrentMap<PlatformKey, PlatformData> platforms = Maps.newConcurrentMap();

    Optional<PlatformData> getPlatform(final PlatformKey key) {
        return Optional.ofNullable(this.platforms.get(key));
    }

    void createOrUpdate(final PlatformData platform) {
        platforms.put(platform.getKey(), platform);
    }

    List<PlatformData> getPlatformsForApplication(final String applicationName) {
        return platforms.keySet().stream()
                .filter(key -> key.getApplicationName().equals(applicationName))
                .map(key -> platforms.get(key))
                .collect(Collectors.toList());
    }

    void delete(final PlatformKey key) {
        platforms.remove(key);
    }

    Collection<PlatformData> getAllPlatforms() {
        return platforms.values();
    }
}
