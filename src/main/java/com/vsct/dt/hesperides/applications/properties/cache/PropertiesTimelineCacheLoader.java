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
