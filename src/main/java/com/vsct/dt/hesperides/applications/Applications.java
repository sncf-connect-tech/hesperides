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

import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.platform.ApplicationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.templating.platform.TimeStampedPlatformData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 25/02/2015.
 */
public interface Applications {
    Optional<PlatformData> getPlatform(PlatformKey platformKey);

    Optional<TimeStampedPlatformData> getPlatform(PlatformKey platformKey, long timestamp);

    PlatformData createPlatform(PlatformData platform);

    PlatformData createPlatformFromExistingPlatform(PlatformData platform, PlatformKey fromPlatformKey);

    PlatformData updatePlatform(PlatformData platform, boolean isCopyingPropertiesForUpdatedModules);

    PropertiesData getProperties(PlatformKey platformKey, String path);

    PropertiesData getProperties(PlatformKey platformKey, String path, long timestamp);

    PropertiesData getSecuredProperties(PlatformKey platformKey, String path, HesperidesPropertiesModel model);

    PropertiesData getSecuredProperties(PlatformKey platformKey, String path, long timestamp, HesperidesPropertiesModel model);

    PropertiesData createOrUpdatePropertiesInPlatform(PlatformKey platformKey, String path, PropertiesData properties, long platformVersionID, String comment);

    InstanceModel getInstanceModel(PlatformKey platformKey, String propertiesPath);

    Collection<PlatformData> getAllPlatforms();

    void delete(PlatformKey key);

    long takeSnapshot(PlatformKey key);

    List<Long> getSnapshots(PlatformKey key);

    PlatformData restoreSnapshot(PlatformKey key, long timestamp);
}
