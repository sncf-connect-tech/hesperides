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

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

/**
 * Created by william_montaz on 10/12/2014.
 */
public class ApplicationsAggregate extends AbstractApplicationsAggregate {
    /**
     * The platform registry holds all platforms instances, it hides how platforms are stored.
     * The properties registry holds all properties sets, it hides how properties are stored.
     */
    private PlatformRegistry pr;

    /**
     * Used to make snapshots, it hides implementation details about how snapshots are stored
     * Since there might be a lot of snapshot, we dont want to keep them inmemory like platformRegistry and propertiesRegistry
     */
    private SnapshotRegistryInterface snapshotRegistryInterface;

    /**
     * Constructor to be used.
     *
     * @param eventBus
     * @param eventStore
     * @param snapshotRegistryInterface
     * @param hesperidesConfiguration
     */
    public ApplicationsAggregate(final EventBus eventBus, final EventStore eventStore,
                                 final SnapshotRegistryInterface snapshotRegistryInterface,
                                 final HesperidesConfiguration hesperidesConfiguration) {
        super(eventBus, eventStore);

        init(eventStore, snapshotRegistryInterface, hesperidesConfiguration);
    }

    public ApplicationsAggregate(final EventBus eventBus, final EventStore eventStore,
                                 final SnapshotRegistryInterface snapshotRegistryInterface,
                                 final HesperidesConfiguration hesperidesConfiguration, final UserProvider userProvider) {
        super(eventBus, eventStore, userProvider);

        init(eventStore, snapshotRegistryInterface, hesperidesConfiguration);
    }

    /**
     * Init component.
     *
     * @param eventStore
     * @param snapshotRegistryInterface
     * @param hesperidesConfiguration
     */
    private void init(final EventStore eventStore, final SnapshotRegistryInterface snapshotRegistryInterface,
                      final HesperidesConfiguration hesperidesConfiguration) {
        HesperidesCacheParameter platformParameterTimeline = null;

        if (hesperidesConfiguration.getCacheConfiguration() != null) {
            platformParameterTimeline = hesperidesConfiguration.getCacheConfiguration().getPlatformTimeline();
        }

        HesperidesCacheParameter platform = null;

        if (hesperidesConfiguration.getCacheConfiguration() != null) {
            platform = hesperidesConfiguration.getCacheConfiguration().getPlatform();
        }

        final long nbEventBeforePersiste = hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste();

        this.snapshotRegistryInterface = snapshotRegistryInterface;
        this.pr = new PlatformRegistry(eventStore, nbEventBeforePersiste, platform, platformParameterTimeline);
    }

    @Override
    protected PlatformRegistryInterface getPlatformRegistry() {
        return pr;
    }

    @Override
    protected PropertiesRegistryInterface getPropertiesRegistry() {
        return pr;
    }

    @Override
    protected SnapshotRegistryInterface getSnapshotRegistry() {
        return this.snapshotRegistryInterface;
    }

    public void removeFromCache(final String applicationName, final String applicationVersion) {
        pr.removeFromCache(applicationName, applicationVersion);
    }

    public void removeAllCache() {
        pr.removeAllCache();
    }

    @Override
    public void regenerateCache() {
        // Nothing
    }
}
