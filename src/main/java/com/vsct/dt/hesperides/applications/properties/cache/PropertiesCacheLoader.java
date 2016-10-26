package com.vsct.dt.hesperides.applications.properties.cache;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.properties.event.PlatformContainer;
import com.vsct.dt.hesperides.storage.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public class PropertiesCacheLoader extends AbstractPropertiesCacheLoader<PlatformKey> {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesCacheLoader.class);

    public PropertiesCacheLoader(final EventStore store, final long nbEventBeforePersiste) {
        super(store, nbEventBeforePersiste);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public PlatformContainer load(final PlatformKey key) throws Exception {
        return loadProperties(key, Long.MAX_VALUE, true);
    }
}
