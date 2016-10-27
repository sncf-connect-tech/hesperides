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

package com.vsct.dt.hesperides.applications.virtual;

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.applications.AbstractApplicationsAggregate;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.SnapshotRegistryInterface;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.Optional;

/**
 * Created by emeric_martineau on 27/05/2016.
 */
public class VirtualApplicationsAggregate extends AbstractApplicationsAggregate {
    /**
     * The platform registry holds all platforms instances, it hides how platforms are stored.
     * The properties registry holds all properties sets, it hides how properties are stored.
     */
    private VirtualPlatformRegistry pr;

    /**
     * Used to make snapshots, it hides implementation details about how snapshots are stored
     * Since there might be a lot of snapshot, we dont want to keep them inmemory like platformRegistry and propertiesRegistry
     */
    private SnapshotRegistryInterface snapshotRegistryInterface;

    public VirtualApplicationsAggregate(final EventStore store) {
        super(new EventBus(), store);

        this.snapshotRegistryInterface = new VirtualSnapshotRegistry();
        this.pr = new VirtualPlatformRegistry();
    }

    public VirtualApplicationsAggregate(final EventStore store, final PlatformData platform,
                                        final Map<String, PropertiesData> propertiesDataMap) {
        this(store);

        pr.createOrUpdatePlatform(platform);
        pr.addProperties(propertiesDataMap);
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

    public Optional<PlatformData> getPlatform() {
        return this.pr.getPlatform((PlatformKey) null);
    }

    public Map<String, PropertiesData> getProperties() {
        return this.pr.getProperties(null, null);
    }

    public void clear() {
        this.pr.clear();
    }

    public void replay(final String stream) {
        super.replay(stream);
    }

    public void replay(final String stream, final long start, final long stop) {
        super.replay(stream, start, stop);
    }

    @Override
    public void regenerateCache() {
        // Nothing
    }
}
