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

package com.vsct.dt.hesperides.templating.packages.cache;

import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.HesperidesSnapshotItem;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.exception.ModuleNotFoundInDatabaseException;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.event.AbstractTemplateCacheLoader;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageContainer;
import com.vsct.dt.hesperides.templating.packages.virtual.VirtualTemplatePackagesAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public class TemplatePackageCacheLoader extends AbstractTemplateCacheLoader<String, TemplatePackageContainer>
        implements TemplatePackageStoragePrefixInterface {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatePackageCacheLoader.class);

    /**
     * Constructor.
     *
     * @param store event store to load event from database
     */
    public TemplatePackageCacheLoader(final EventStore store, final long nbEventBeforePersiste) {
        super(store, nbEventBeforePersiste);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected TemplatePackageContainer createEventBuilder() {
        return new TemplatePackageContainer();
    }

    @Override
    protected String getObjectLoadName() {
        return "template package";
    }

    @Override
    public TemplatePackageContainer load(final String namespace) throws ModuleNotFoundInDatabaseException {
        return loadTemplate(namespace);
    }

    public TemplatePackageContainer loadTemplate(final String namespace) {
        final String loadObjectName = getObjectLoadName();

        getLogger().debug("Load {} with namespace '{}' from store", loadObjectName, namespace);
        final String redisKey = generateDbKey(new ModuleKey(namespace));

        // First seach last snapshot
        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = getStore().findLastSnapshot(redisKey);

        TemplatePackageContainer templatePackageContainer;

        if (hesperidesSnapshotItem.isPresent()) {
            final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

            templatePackageContainer = (TemplatePackageContainer) snapshot.getSnapshot();

            // If snapshot is done on last event, do nothing
            if (snapshot.getStreamNbEvents() <= snapshot.getCacheNbEvents()) {
                return templatePackageContainer;
            }

            final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                    = new VirtualTemplatePackagesAggregate(
                    getStore(),
                    templatePackageContainer.loadAllTemplate());

            final long start = snapshot.getCacheNbEvents();
            final long stop = snapshot.getStreamNbEvents();

            virtualTemplatePackagesAggregate.replay(redisKey, start, stop);

            updateTemplatePackagesContainer(namespace, templatePackageContainer, virtualTemplatePackagesAggregate);
        } else {
                // Module builder
                templatePackageContainer = createEventBuilder();

                final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                        = new VirtualTemplatePackagesAggregate(getStore());

                virtualTemplatePackagesAggregate.replay(redisKey);

                updateTemplatePackagesContainer(namespace, templatePackageContainer, virtualTemplatePackagesAggregate);
        }

        // Can't return null !!!!
        return templatePackageContainer;
    }

    /**
     * Update tempalte package container.
     *
     * @param namespace
     * @param templatePackageContainer
     * @param virtualTemplatePackagesAggregate
     */
    private void updateTemplatePackagesContainer(final String namespace,
                                                 final TemplatePackageContainer templatePackageContainer,
                                                 final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate) {
        Set<Template> listTemplate = virtualTemplatePackagesAggregate.getAllTemplates(
                new TemplatePackageKey(namespace));

        if (listTemplate != null) {
            listTemplate.stream().forEach(t -> templatePackageContainer.addTemplate(t));
        }
    }

    /**
     * Return list of modules.
     *
     * @return list of modules
     */
    @Override
    public List<Template> getAllTemplates() {
        final String loadObjectName = getObjectLoadName();

        getLogger().debug("Load all {} from store.", loadObjectName);

        // Redis key pattern to search all application template
        final String redisKey = String.format("%s-*",
                getStreamPrefix());
        // All application template redis key.
        final Set<String> templates = getStore().getStreamsLike(redisKey);
        // List of template return by method
        final List<Template> listTemplates = new ArrayList<>(templates.size());

        final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                = new VirtualTemplatePackagesAggregate(getStore());

        // Current namespace of template package
        String namespace;
        // Template package container from cache and replay event
        TemplatePackageContainer container;
        // Iterator of template cause we get all platform from vitrualTemplatePackageAggregate.
        // You can only have one TemplatePackage.
        Iterator<Template> itTemplate;

        for (String templateStreamName : templates) {
            getLogger().debug("Load {} from store associate with key '{}' for {}.", loadObjectName,
                    templateStreamName, loadObjectName);

            virtualTemplatePackagesAggregate.clear();

            // First event is always create event.
            // That mean with you don't need replay event to know with template package is associate to redis stream
            virtualTemplatePackagesAggregate.replay(templateStreamName, 0, 1);

            // Get template to read namespace
            itTemplate = virtualTemplatePackagesAggregate.getAllTemplates().iterator();

            if (itTemplate.hasNext()) {
                namespace = itTemplate.next().getNamespace();

                container = loadTemplate(namespace);

                listTemplates.addAll(container.loadAllTemplate());
            }
        }

        getLogger().debug("All {} are loaded.", loadObjectName);

        // Can't return null !!!!
        return listTemplates;
    }
}
