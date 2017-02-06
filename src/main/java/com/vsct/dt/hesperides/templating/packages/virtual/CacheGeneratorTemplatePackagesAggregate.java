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

package com.vsct.dt.hesperides.templating.packages.virtual;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.*;
import com.vsct.dt.hesperides.templating.packages.cache.TemplatePackageCacheLoader;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageContainer;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public class CacheGeneratorTemplatePackagesAggregate extends AbstractTemplatePackagesAggregate {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheGeneratorTemplatePackagesAggregate.class);

    /**
     * Nb event before store cache for force cache system.
     */
    private final long nbEventBeforePersiste;

    /**
     * Model.
     */
    private final Models models;

    /**
     * Template registry.
     */
    private VirtualTemplateRegistry templateRegistry;

    /**
     * Internal structure holding in memory state
     */
    private TemplatePackageCacheLoader templatePackageCacheLoader;

    /**
     * Count nb event for cache regeneration.
     */
    private long count;

    /**
     * Number of snapshot cache.
     */
    private long snapshotCacheCount;

    /**
     * Construtor.
     *
     * @param store
     * @param hesperidesConfiguration
     */
    public CacheGeneratorTemplatePackagesAggregate(final EventStore store,
                                                   final HesperidesConfiguration hesperidesConfiguration) {
        super(new EventBus(), store);

        this.templateRegistry = new VirtualTemplateRegistry();
        this.models = new Models(this.templateRegistry);
        this.nbEventBeforePersiste
                = hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste();

        this.templatePackageCacheLoader = new TemplatePackageCacheLoader(store, nbEventBeforePersiste);
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.templateRegistry;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void replay(final String stream) {
        this.templateRegistry.clear();

        super.replay(stream);
    }

    public void replay(final String stream, final long start, final long stop) {
        this.templateRegistry.clear();

        super.replay(stream, start, stop);
    }

    @Override
    @Subscribe
    public void replayTemplateCreatedEvent(final TemplateCreatedEvent event) {
        super.replayTemplateCreatedEvent(event);

        increaseCounter(new TemplatePackageKey(event.getCreated().getNamespace()));
    }

    @Override
    @Subscribe
    public void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event) {
        super.replayTemplateUpdatedEvent(event);

        increaseCounter(new TemplatePackageKey(event.getUpdated().getNamespace()));
    }

    @Override
    @Subscribe
    public void replayTemplateDeletedEvent(final TemplateDeletedEvent event) {
        super.replayTemplateDeletedEvent(event);

        increaseCounter(new TemplatePackageKey(event.getNamespace()));
    }

    @Override
    @Subscribe
    public void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event) {
        super.replayTemplatePackageDeletedEvent(event);

        increaseCounter(new TemplatePackageKey(event.getPackageName(),
                new HesperidesVersion(event.getPackageVersion(), event.isWorkingCopy())));
    }

    public void clear() {
        this.templateRegistry.clear();
    }

    private void increaseCounter(final TemplatePackageKey key) {
        this.count++;

        if (this.count % this.nbEventBeforePersiste == 0) {
            this.snapshotCacheCount++;

            LOGGER.debug("Store new instance (#{}) of cache for {}", this.snapshotCacheCount, key);

            final TemplatePackageContainer snapshot = new TemplatePackageContainer();

            this.templateRegistry.getAllTemplates(key).stream().forEach(t -> snapshot.addTemplate(t));

            this.templatePackageCacheLoader.forceSaveSnapshot(key.getNamespace(), snapshot, this.count);
        }
    }
}
