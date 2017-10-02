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

package com.vsct.dt.hesperides.templating.modules.cache;

import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.HesperidesSnapshotItem;
import com.vsct.dt.hesperides.templating.modules.AbstractModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.event.ModuleContainer;
import com.vsct.dt.hesperides.templating.modules.exception.ModuleNotFoundInDatabaseException;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.event.AbstractTemplateCacheLoader;
import com.vsct.dt.hesperides.templating.modules.virtual.VirtualModulesAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 15/01/2016.
 */
public class ModuleCacheLoader extends AbstractTemplateCacheLoader<ModuleKey, ModuleContainer> implements ModuleStoragePrefixInterface {
    private interface Callback {
        void moduleFound(Module module, Set<Template> templates);
    }


    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleCacheLoader.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Constructor.
     *
     * @param store store of event
     */
    public ModuleCacheLoader(final EventStore store, final long nbEventBeforePersiste) {
        super(store, nbEventBeforePersiste);
    }

    @Override
    protected ModuleContainer createEventBuilder() {
        return new ModuleContainer();
    }

    @Override
    protected String getObjectLoadName() {
        return "module";
    }

    @Override
    public ModuleContainer load(final ModuleKey moduleKey) throws ModuleNotFoundInDatabaseException {
        LOGGER.debug("Load module with key '{}'.", moduleKey);

        // Redis key pattern to search all application platform
        final String redisKey = generateDbKey(moduleKey);

        // First seach last snapshot
        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = getStore().findLastSnapshot(redisKey);

        ModuleContainer moduleContainer;

        if (hesperidesSnapshotItem.isPresent()) {
            final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

            moduleContainer = (ModuleContainer) snapshot.getSnapshot();

            // If snapshot is done on last event, do nothing
            if (snapshot.getStreamNbEvents() <= snapshot.getCacheNbEvents()) {
                return moduleContainer;
            }

            final VirtualModulesAggregate virtualModulesAggregate = new VirtualModulesAggregate(getStore(),
                    moduleContainer.getModule(), moduleContainer.loadAllTemplate());

            virtualModulesAggregate.replay(redisKey, snapshot.getCacheNbEvents(), snapshot.getStreamNbEvents());

            final Optional<Module> module = virtualModulesAggregate.getModule(moduleKey);

            updateModuleContainer(moduleKey, moduleContainer, virtualModulesAggregate, module);
        } else {
            // Module builder
            moduleContainer = createEventBuilder();

            final VirtualModulesAggregate virtualModulesAggregate = new VirtualModulesAggregate(getStore());

            virtualModulesAggregate.replay(redisKey);

            virtualModulesAggregate.getModule(moduleKey);

            // Warning : must never return null  !
            final Optional<Module> module = virtualModulesAggregate.getModule(moduleKey);

            updateModuleContainer(moduleKey, moduleContainer, virtualModulesAggregate, module);
        }

        // Can't return null !!!!
        return moduleContainer;
    }

    /**
     * Update module container.
     *
     * @param moduleKey
     * @param moduleContainer
     * @param virtualModulesAggregate
     * @param module
     * @throws ModuleNotFoundInDatabaseException
     */
    private void updateModuleContainer(final ModuleKey moduleKey, final ModuleContainer moduleContainer,
                                       final VirtualModulesAggregate virtualModulesAggregate,
                                       final Optional<Module> module) throws ModuleNotFoundInDatabaseException {
        if (module.isPresent()) {
            moduleContainer.setModule(module.get());

            final List<Template> templates = virtualModulesAggregate.getAllTemplates(moduleKey);

            if (templates != null) {
                templates.stream().forEach(t -> moduleContainer.addTemplate(t));
            }
        } else {
            throw new ModuleNotFoundInDatabaseException();
        }
    }

    /**
     * Return list of modules.
     *
     * @return list of modules
     */
    public List<Module> getAllModules() {
        return getAllModules(null);
    }

    /**
     * Retrieve module key with redis key.
     *
     * @param virtualModulesAggregate
     * @param redisKey
     * @return
     */
    private Optional<ModuleContainer> retreiveModule(final VirtualModulesAggregate virtualModulesAggregate, final String redisKey) {
        virtualModulesAggregate.clear();

        // First event is always create event.
        // That mean with you don't need replay event to know with module is associate to redis stream
        virtualModulesAggregate.replay(redisKey, 0, 1);

        final Iterator<Module> itModule = virtualModulesAggregate.getAllModules().iterator();

        if (itModule.hasNext()) {
            final Module module = itModule.next();

            try {
                return Optional.of(load(module.getKey()));
            } catch (ModuleNotFoundInDatabaseException e) {
                LOGGER.error("When reload all modules, we can't load module in stream {}", redisKey);
            }
        }

        return Optional.empty();
    }

    /**
     * Store object in snapshot.
     *
     * @param moduleKey key of cache (same as cache.get(K))
     * @param object object
     */
    public void saveSnapshot(final ModuleKey moduleKey, final ModuleContainer object) {
        // Don't store snapshot of release, cause release contain only one event
        if (moduleKey.isWorkingCopy()) {
            final String redisKey = generateDbKey(moduleKey);

            // Now store snapshot
            getStore().storeSnapshot(redisKey, object, getNbEventBeforePersiste());
        }
    }

    /**
     * Store object in snapshot.
     *
     * @param moduleKey key of cache (same as cache.get(K))
     * @param object object
     */
    public void forceSaveSnapshot(final ModuleKey moduleKey, final ModuleContainer object, final long nbEvent) {
        // Don't store snapshot of release, cause release contain only one event
        if (moduleKey.isWorkingCopy()) {
            final String redisKey = generateDbKey(moduleKey);

            // Now store snapshot
            getStore().createSnapshot(redisKey, object, nbEvent);
        }
    }

    /**
     * Return list of modules.
     *
     * @return list of modules
     */
    private List<Module> getAllModules(final Callback callback) {
        LOGGER.debug("Load all modules from store.");

        // Redis key pattern to search all application platform
        final String redisKey = String.format("%s-*",
                getStreamPrefix());
        // All application module redis key.
        final Set<String> modules = getStore().getStreamsLike(redisKey);
        // List of platform return by method
        final List<Module> listModule = new ArrayList<>(modules.size());
        // Module
        Optional<ModuleContainer> optContainer;
        ModuleContainer container;

        final VirtualModulesAggregate virtualModulesAggregate = new VirtualModulesAggregate(getStore());

        for (String moduleRedisKey : modules) {
            LOGGER.debug("Load module from store associate with key '{}'.", moduleRedisKey);

            optContainer = retreiveModule(virtualModulesAggregate, moduleRedisKey);

            if (optContainer.isPresent()) {
                container = optContainer.get();

                listModule.add(container.getModule());

                if (callback != null) {
                    callback.moduleFound(container.getModule(), container.loadAllTemplate());
                }
            }
        }

        LOGGER.debug("All modules are loaded.");

        // Can't return null !!!!
        return listModule;
    }

    /**
     * Return list of modules.
     *
     * @return list of modules
     */
    @Override
    public List<Template> getAllTemplates() {
        final List<Template> listTemplates = new ArrayList<>();

        getAllModules((module, templates) ->
            listTemplates.addAll(templates)
        );

        // Can't return null !!!!
        return listTemplates;
    }
}
