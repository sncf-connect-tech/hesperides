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

package com.vsct.dt.hesperides.applications.properties.cache;

import com.vsct.dt.hesperides.applications.PlatformTimelineKey;
import com.vsct.dt.hesperides.applications.properties.event.PlatformContainer;
import com.vsct.dt.hesperides.storage.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public class PropertiesTimelineCacheLoader extends AbstractPropertiesCacheLoader<PlatformTimelineKey> {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesTimelineCacheLoader.class);

    public PropertiesTimelineCacheLoader(final EventStore store) {
        super(store, Long.MAX_VALUE);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public PlatformContainer load(final PlatformTimelineKey key) throws Exception {
        return loadProperties(key.getPlatformKey(), key.getTimestamp(), false);
    }
}
