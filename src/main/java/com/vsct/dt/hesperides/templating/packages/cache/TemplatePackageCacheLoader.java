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
import java.util.List;
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
        final String loadObjectName = getObjectLoadName();

        getLogger().debug("Load {} with namespace '{}' from store", loadObjectName, namespace);
        final String redisKey = generateDbKey(new ModuleKey(namespace));

        // First seach last snapshot
        final HesperidesSnapshotItem hesperidesSnapshotItem = getStore().findLastSnapshot(redisKey);

        TemplatePackageContainer templatePackageContainer;

        if (hesperidesSnapshotItem == null
                || hesperidesSnapshotItem.getCurrentNbEvents() < hesperidesSnapshotItem.getNbEvents()) {
            // Module builder
            templatePackageContainer = createEventBuilder();

            final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                    = new VirtualTemplatePackagesAggregate(getStore());

            virtualTemplatePackagesAggregate.replay(redisKey);

            updateTemplatePackagesContainer(namespace, templatePackageContainer, virtualTemplatePackagesAggregate);
        } else {
            templatePackageContainer = (TemplatePackageContainer) hesperidesSnapshotItem.getSnapshot();

            if (hesperidesSnapshotItem.getCurrentNbEvents() > hesperidesSnapshotItem.getNbEvents()) {
                final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                        = new VirtualTemplatePackagesAggregate(
                        getStore(),
                        templatePackageContainer.loadAllTemplate());

                final long start = hesperidesSnapshotItem.getNbEvents();
                final long stop = hesperidesSnapshotItem.getCurrentNbEvents();

                virtualTemplatePackagesAggregate.replay(redisKey, start, stop);

                updateTemplatePackagesContainer(namespace, templatePackageContainer, virtualTemplatePackagesAggregate);
            }
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

        // Redis key pattern to search all application tempalte
        final String redisKey = String.format("%s-*",
                getStreamPrefix());
        // All application template redis key.
        final Set<String> templates = getStore().getStreamsLike(redisKey);
        // List of template return by method
        final List<Template> listTemplates = new ArrayList<>(templates.size());

        final VirtualTemplatePackagesAggregate virtualTemplatePackagesAggregate
                = new VirtualTemplatePackagesAggregate(getStore());

        for (String templateRedisKey : templates) {
            getLogger().debug("Load {} from store associate with key '{}' for {}.", loadObjectName,
                    templateRedisKey, loadObjectName);

            virtualTemplatePackagesAggregate.replay(templateRedisKey);

            virtualTemplatePackagesAggregate.withAll(t -> listTemplates.add(t));

            virtualTemplatePackagesAggregate.clear();
        }

        getLogger().debug("All {} are loaded.", loadObjectName);

        // Can't return null !!!!
        return listTemplates;
    }
}
