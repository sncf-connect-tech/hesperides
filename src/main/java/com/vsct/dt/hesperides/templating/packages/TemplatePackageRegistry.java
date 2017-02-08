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


import com.google.common.cache.LoadingCache;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.cache.TemplatePackageCacheLoader;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageContainer;
import com.vsct.dt.hesperides.util.HesperidesCacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public class TemplatePackageRegistry implements TemplateRegistryInterface {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatePackageRegistry.class);

    /**
     * Lazy loader.
     */
    private final TemplatePackageCacheLoader templateCacheLoader;

    /**
     * Cache contain module or load it.
     */
    private final LoadingCache<String, TemplatePackageContainer> cache;

    /**
     * Constructor.
     *
     * @param store store of event for lazy load
     * @param nbEventBeforePersiste nb event before store cache
     * @param config config of cache
     */
    public TemplatePackageRegistry(final EventStore store, final long nbEventBeforePersiste,
                                   final HesperidesCacheParameter config) {
        this.templateCacheLoader = new TemplatePackageCacheLoader(store, nbEventBeforePersiste);


        this.cache = HesperidesCacheBuilder.newBuilder(config, (key, value) -> ((TemplatePackageContainer) value).loadAllTemplate().size())
                .build(this.templateCacheLoader);
    }


    @Override
    public Collection<Template> allTemplates() {
        LOGGER.debug("Get all template package from store.");

        return this.templateCacheLoader.getAllTemplates();
    }

    @Override
    public Optional<Template> getTemplate(final String namespace, final String name) {
        try {
            LOGGER.debug("Search package template '{}' for namespace '{}'.", name, namespace);

            final TemplatePackageContainer templateCache = this.cache.get(namespace);

            return Optional.ofNullable(templateCache.getTemplate(name));
        } catch (final ExecutionException e) {
            LOGGER.debug("Can't find package template '{}' for namespace '{}'.", name, namespace);

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
        LOGGER.debug("Add package template '{}'.", template);

        TemplatePackageContainer templateCache;

        try {
            templateCache = this.cache.get(template.getNamespace());

            templateCache.addTemplate(template);
        } catch (final ExecutionException e) {
            // When not found in database -> create
            templateCache = new TemplatePackageContainer();

            templateCache.addTemplate(template);
        }

        // Write snapshot
        templateCacheLoader.saveSnapshot(template.getNamespace(), templateCache);
    }

    @Override
    public void deleteTemplate(final String namespace, final String name) {
        TemplatePackageContainer templateCache;

        try {
            templateCache = this.cache.get(namespace);
        } catch (final ExecutionException e) {
            templateCache = null;
        }

        // Delete template only if in cache
        if (templateCache != null) {
            templateCache.removeTemplate(name);

            // Write snapshot
            templateCacheLoader.saveSnapshot(namespace, templateCache);
        }
    }

    @Override
    public Set<Template> getAllTemplatesForNamespace(final String namespace) {
        try {
            LOGGER.debug("Get all package template for namespace '{}'.", namespace);

            final TemplatePackageContainer templateCache = this.cache.get(namespace);

            return templateCache.loadAllTemplate();
        } catch (final ExecutionException e) {
            LOGGER.debug("Can't get all package template for namespace '{}'.", namespace);

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
        return this.templateCacheLoader.isNamespaceExist(namespace);
    }

    @Override
    public void removeFromCache(final TemplatePackageKey packageKey) {
        this.cache.invalidate(packageKey.getNamespace());
    }

    @Override
    public void removeAllCache() {
        this.cache.invalidateAll();
    }
}
