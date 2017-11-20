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

package com.vsct.dt.hesperides.applications;

import com.vsct.dt.hesperides.templating.platform.PlatformData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by emeric_martineau on 27/04/2016.
 */
public interface PlatformRegistryInterface {
    Optional<PlatformData> getPlatform(PlatformKey key);

    Optional<PlatformData> getPlatform(PlatformTimelineKey key);

    void createOrUpdatePlatform(PlatformData platform);

    void deletePlatform(PlatformKey key);

    Collection<PlatformData> getAllPlatforms();

    /**
     * Remove item from cache.
     *
     * @param key
     */
    void removeFromCache(PlatformKey key);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
