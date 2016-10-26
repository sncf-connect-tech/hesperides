package com.vsct.dt.hesperides.templating.packages.virtual;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.*;
import com.vsct.dt.hesperides.templating.modules.cache.ModuleCacheLoader;
import com.vsct.dt.hesperides.templating.modules.event.ModuleContainer;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.modules.virtual.VirtualModuleRegistry;
import com.vsct.dt.hesperides.templating.packages.AbstractTemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public class CacheGeneratorModuleAggregate extends AbstractModulesAggregate {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheGeneratorModuleAggregate.class);

    /**
     * Nb event before store cache for force cache system.
     */
    private final long nbEventBeforePersiste;

    /**
     * Module registry.
     */
    private VirtualModuleRegistry moduleRegistry;

    /**
     * Internal structure holding in memory state
     */
    private ModuleCacheLoader moduleCacheLoader;

    /**
     * Template package aggregate.
     */
    private VirtualTemplatePackagesAggregate tpa;

    /**
     * Model.
     */
    private final Models models;

    /**
     * Count nb event for cache regeneration.
     */
    private long count;

    /**
     * Number of snapshot cache.
     */
    private long snapshotCacheCount;

    /**
     * Constructor.
     *
     * @param store
     * @param hesperidesConfiguration
     */
    public CacheGeneratorModuleAggregate(final EventStore store,
                                         final HesperidesConfiguration hesperidesConfiguration) {
        super(new EventBus(), store);

        this.moduleRegistry = new VirtualModuleRegistry();
        this.models = new Models(this.moduleRegistry);
        this.tpa = new VirtualTemplatePackagesAggregate(store);
        this.nbEventBeforePersiste
                = hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste();

        this.moduleCacheLoader = new ModuleCacheLoader(store, nbEventBeforePersiste);
    }

    @Override
    protected ModuleRegistryInterface getModuleRegistry() {
        return this.moduleRegistry;
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.moduleRegistry;
    }

    @Override
    protected AbstractTemplatePackagesAggregate getTemplatePackages() {
        return this.tpa;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void replay(final String stream) {
        this.count = 0;
        this.snapshotCacheCount = 0;

        this.moduleRegistry.clear();

        super.replay(stream);
    }

    @Override
    @Subscribe
    public void replayModuleCreatedEvent(final ModuleCreatedEvent event) {
        super.replayModuleCreatedEvent(event);

        increaseCounter(event.getModuleCreated().getKey());
    }

    @Override
    @Subscribe
    public void replayModuleWorkingCopyUpdatedEvent(final ModuleWorkingCopyUpdatedEvent event) {
        super.replayModuleWorkingCopyUpdatedEvent(event);

        increaseCounter(event.getUpdated().getKey());
    }

    @Override
    @Subscribe
    public void replayModuleTemplateCreatedEvent(final ModuleTemplateCreatedEvent event) {
        super.replayModuleTemplateCreatedEvent(event);

        increaseCounter(new ModuleKey(event.getCreated().getNamespace()));
    }

    @Override
    @Subscribe
    public void replayModuleTemplateUpdatedEvent(final ModuleTemplateUpdatedEvent event) {
        super.replayModuleTemplateUpdatedEvent(event);

        increaseCounter(new ModuleKey(event.getUpdated().getNamespace()));
    }

    @Override
    @Subscribe
    public void replayModuleTemplateDeletedEvent(final ModuleTemplateDeletedEvent event) {
        super.replayModuleTemplateDeletedEvent(event);

        increaseCounter(new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion()));
    }

    @Override
    @Subscribe
    public void replayModuleDeletedEvent(final ModuleDeletedEvent event) {
        super.replayModuleDeletedEvent(event);

        increaseCounter(new ModuleKey(event.getModuleName(),
                new HesperidesVersion(event.getModuleVersion(), event.isWorkingCopy())
        ));
    }

    private void increaseCounter(final ModuleKey key) {
        this.count++;

        if (this.count % this.nbEventBeforePersiste == 0) {
            this.snapshotCacheCount++;

            LOGGER.debug("Store new instance (#{}) of cache for {}", this.snapshotCacheCount, key);

            final ModuleContainer snapshot = new ModuleContainer();

            if (this.moduleRegistry.getModule(key).isPresent()) {
                snapshot.setModule(this.moduleRegistry.getModule(key).get());
            }

            this.moduleRegistry.getAllTemplates(key).stream().forEach(t -> snapshot.addTemplate(t));

            this.moduleCacheLoader.forceSaveSnapshot(key, snapshot, this.count);
        }
    }
}
