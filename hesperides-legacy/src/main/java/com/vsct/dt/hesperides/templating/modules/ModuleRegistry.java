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

package com.vsct.dt.hesperides.templating.modules;

import com.google.common.cache.LoadingCache;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.modules.cache.ModuleCacheLoader;
import com.vsct.dt.hesperides.templating.modules.event.ModuleContainer;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.util.HesperidesCacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
* Created by william_montaz on 29/04/2015.
*/
class ModuleRegistry implements ModuleRegistryInterface, TemplateRegistryInterface {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleRegistry.class);

    /**
     * Lazy load.
     */
    private final ModuleCacheLoader moduleCacheLoader;

    /**
     * Cache contain module or load it.
     */
    private final LoadingCache<ModuleKey, ModuleContainer> cache;

    /**
     * Constructor.
     *
     * @param store store of event for lazy load
     * @param nbEventBeforePersiste nb event before store cache
     * @param config cache config
     */
    public ModuleRegistry(final EventStore store, final long nbEventBeforePersiste,
                          final HesperidesCacheParameter config) {
        this.moduleCacheLoader = new ModuleCacheLoader(store, nbEventBeforePersiste);

        this.cache = HesperidesCacheBuilder.newBuilder(config)
                .build(this.moduleCacheLoader);
    }

    @Override
    public void createOrUpdateModule(final Module module) {
        LOGGER.debug("Add module '{}' in cache.", module);

        final ModuleKey moduleKey = module.getKey();

        ModuleContainer moduleContainer;

        try {
            moduleContainer = this.cache.get(moduleKey);
        } catch (final ExecutionException e) {
            // Module can't be load, module not exists
            moduleContainer = new ModuleContainer();
        }

        // Update module
        moduleContainer.setModule(module);

        // Write snapshot
        moduleCacheLoader.saveSnapshot(moduleKey, moduleContainer);
    }

    @Override
    public boolean existsModule(final ModuleKey key) {
        return getModule(key).isPresent();
    }

    @Override
    public Optional<Module> getModule(final ModuleKey key) {
        try {
            LOGGER.debug("Search module '{}' in cache.", key);

            final ModuleContainer moduleContainer = this.cache.get(key);

            return Optional.ofNullable(moduleContainer.getModule());
        } catch (final ExecutionException e) {
            LOGGER.debug("Can't find module '{}' in cache.", key);

            // Cache not found
            return Optional.empty();
        }
    }

    @Override
    public void deleteModule(final ModuleKey key) {
        try {
            ModuleContainer moduleContainer;

            // Load snapshot
            moduleContainer = this.cache.get(key);

            // Update module
            moduleContainer.clear();

            // Write snapshot
            moduleCacheLoader.saveSnapshot(key, moduleContainer);
        } catch (final ExecutionException e) {
            // ??? normaly not !
        }
    }

    @Override
    public Collection<Module> getAllModules() {
        LOGGER.debug("Get all modules from store.");

        return this.moduleCacheLoader.getAllModules();
    }

    @Override
    public void removeFromCache(ModuleKey key) {
        this.cache.invalidate(key);
    }

    @Override
    public void removeAllCache() {
        this.cache.invalidateAll();
    }

    @Override
    public Collection<Template> allTemplates() {
        return this.moduleCacheLoader.getAllTemplates();
    }

    @Override
    public Optional<Template> getTemplate(final String namespace, final String name) {
        try {
            final ModuleContainer moduleContainer = this.cache.get(new ModuleKey(namespace));

            return Optional.ofNullable(moduleContainer.getTemplate(name));
        } catch (final ExecutionException e) {
            // Module not found
            return Optional.empty();
        }
    }

    @Override
    public Optional<Template> getTemplate(final TemplatePackageKey packageKey, final String name) {
        return getTemplate(packageKey.getNamespace(), name);
    }

    @Override
    public boolean existsTemplate(final String namespace, final String name) {
        return getTemplate(namespace, name).isPresent();
    }

    @Override
    public void createOrUpdateTemplate(final Template template) {
        ModuleContainer moduleContainer;

        try {
            final ModuleKey key = new ModuleKey(template.getNamespace());

            moduleContainer = this.cache.get(key);

            // Update cache
            moduleContainer.addTemplate(template);

            // Write snapshot
            moduleCacheLoader.saveSnapshot(key, moduleContainer);
        } catch (final ExecutionException e) {
            // Not possible
        }
    }

    @Override
    public void deleteTemplate(final String namespace, final String name) {
        ModuleContainer moduleContainer;

        try {
            final ModuleKey key = new ModuleKey(namespace);

            moduleContainer = this.cache.get(key);

            // Update cache
            moduleContainer.removeTemplate(name);

            // Write snapshot
            moduleCacheLoader.saveSnapshot(key, moduleContainer);
        } catch (final ExecutionException e) {
            // Not possible
        }
    }

    @Override
    public Set<Template> getAllTemplatesForNamespace(final String namespace) {
        try {
            final ModuleContainer moduleContainer = this.cache.get(new ModuleKey(namespace));

            return moduleContainer.loadAllTemplate();
        } catch (final ExecutionException e) {
            // Module not found
            return new HashSet<>();
        }
    }

    @Override
    public Set<Template> getAllTemplates(TemplatePackageKey packageKey) {
        return getAllTemplatesForNamespace(packageKey.getNamespace());
    }

    @Override
    public boolean templateHasNamespace(final String namespace) {
        return this.moduleCacheLoader.isNamespaceExist(namespace);
    }

    @Override
    public void removeFromCache(final TemplatePackageKey packageKey) {
        // Nothing
    }
}
