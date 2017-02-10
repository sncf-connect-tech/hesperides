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

package com.vsct.dt.hesperides.templating.packages;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

/**
 * Service used to manage templates as "packs".
 * There is no object representing the pack.
 * Templates belong to the same "TemplatePackage" through namespacing
 * Created by william_montaz on 24/11/2014.
 */
public class TemplatePackagesAggregate extends AbstractTemplatePackagesAggregate {
    /**
     * Internal structure holding in memory state
     */
    private TemplateRegistryInterface templateRegistry;

    /**
     * Helper class used to return a template model
     */
    private Models           models;

    /**
     * Nb event before store cache for force cache system.
     */
    private long nbEventBeforePersiste;

    /**
     * Convenient class that wraps the thread executor of the aggregate.
     */
    private ExecutorService singleThreadPool;

    /**
     * Constructor using no UserProvider (used when no loggin was possible)
     * @param eventBus  The {@link com.google.common.eventbus.EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param hesperidesConfiguration hesperides configuration
     */
    public TemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore,
                                     final HesperidesConfiguration hesperidesConfiguration) {
        super(eventBus, eventStore);

        initTemplateAggregate(eventStore, hesperidesConfiguration);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus The {@link com.google.common.eventbus.EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param userProvider A {@link com.vsct.dt.hesperides.storage.UserProvider} that indicates which user is performing the request
     * @param hesperidesConfiguration Hesperides configuration
     */
    public TemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore,
                                     final UserProvider userProvider,
                                     final HesperidesConfiguration hesperidesConfiguration) {
        super(eventBus, eventStore, userProvider);

        initTemplateAggregate(eventStore, hesperidesConfiguration);
    }

    /**
     * Init module.
     *
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param hesperidesConfiguration {@link com.vsct.dt.hesperides.HesperidesConfiguration} configuration hesperides
     */
    private void initTemplateAggregate(final EventStore eventStore,
                                       final HesperidesConfiguration hesperidesConfiguration) {
        HesperidesCacheParameter templateParameter = null;

        if (hesperidesConfiguration.getCacheConfiguration() != null) {
            templateParameter = hesperidesConfiguration.getCacheConfiguration().getTemplatePackage();
        }

        this.nbEventBeforePersiste
                = hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste();

        this.templateRegistry = new TemplatePackageRegistry(eventStore, nbEventBeforePersiste
                , templateParameter);
        this.models = new Models(this.templateRegistry);

        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat(NAME + "-%d")
                .build();

        this.singleThreadPool = Executors.newFixedThreadPool(1, threadFactory);
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.templateRegistry;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void removeFromCache(final String name, final String version, final boolean isWorkingCopy) {
        this.templateRegistry.removeFromCache(new TemplatePackageKey(name, version, isWorkingCopy));
    }

    public void removeAllCache() {
        this.templateRegistry.removeAllCache();
    }

    @Override
    public void regenerateCache() {
        // Nothing
    }

    @Override
    protected ExecutorService executorService() {
        return this.singleThreadPool;
    }
}
