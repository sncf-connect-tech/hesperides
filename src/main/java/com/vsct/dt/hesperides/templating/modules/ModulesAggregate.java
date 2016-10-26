package com.vsct.dt.hesperides.templating.modules;

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.AbstractTemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesVersion;

/**
 * Service used to manage modules and related templates.
 * Templates are loosely tied to modules (only via the namespace attributes).
 * This helps evolving the model defining how templates are tied together.
 * Created by william_montaz on 02/12/2014.
 */
public class ModulesAggregate extends AbstractModulesAggregate {

    /**
     * Helper class to generate properties model from templates
     */
    private Models models;

    /**
     * Inner class storing modules (InMemory state)
     */
    private ModuleRegistryInterface moduleRegistry;

    /**
     * Inner class storing tempaltes related to modules (InMemory State)
     */
    private TemplateRegistryInterface templateRegistry;

    /**
     * TemplatePackage aggregate used to retrieve templates associated to a "techno"
     */
    private TemplatePackagesAggregate templatePackages;

    /**
     * Constructor using no particular UserProvider (used when there was no authentication)
     * @param eventBus {@link com.google.common.eventbus.EventBus} used to propagate events
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param templatePackages {@link com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate} used to get techno related tempaltes
     * @param hesperidesConfiguration hesperides configuration
     */
    public ModulesAggregate(final EventBus eventBus, final EventStore eventStore,
                            final TemplatePackagesAggregate templatePackages,
                            final HesperidesConfiguration hesperidesConfiguration) {
        super(eventBus, eventStore);

        initModulesAggregate(eventStore, templatePackages,
                hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste(),
                hesperidesConfiguration);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus {@link com.google.common.eventbus.EventBus} used to propagate events
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param templatePackages {@link com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate} used to get techno related tempaltes
     * @param userProvider {@link com.vsct.dt.hesperides.storage.UserProvider} used to get information about current user
     * @param hesperidesConfiguration Hesperides configuration
     */
    public ModulesAggregate(final EventBus eventBus, final EventStore eventStore, final TemplatePackagesAggregate templatePackages,
                            final UserProvider userProvider, final HesperidesConfiguration hesperidesConfiguration) {
        super(eventBus, eventStore, userProvider);

        initModulesAggregate(eventStore, templatePackages,
                hesperidesConfiguration.getCacheConfiguration().getNbEventBeforePersiste(), hesperidesConfiguration);
    }

    /**
     * Init module.
     *
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param templatePackages {@link com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate} used to get techno related tempaltes
     * @param nbEventBeforePersiste nb event before store cache
     * @param hesperidesConfiguration {@link com.vsct.dt.hesperides.HesperidesConfiguration} configuration hesperides
     */
    private void initModulesAggregate(final EventStore eventStore, final TemplatePackagesAggregate templatePackages,
                                      final long nbEventBeforePersiste,
                                      final HesperidesConfiguration hesperidesConfiguration) {
        this.templatePackages = templatePackages;

        HesperidesCacheParameter moduleParameter = null;

        if (hesperidesConfiguration.getCacheConfiguration() != null) {
            moduleParameter = hesperidesConfiguration.getCacheConfiguration().getModule();
        }

        final ModuleRegistry mr = new ModuleRegistry(eventStore, nbEventBeforePersiste, moduleParameter);

        this.moduleRegistry = mr;
        this.templateRegistry = mr;
        this.models = new Models(templateRegistry);
    }

    @Override
    protected ModuleRegistryInterface getModuleRegistry() {
        return this.moduleRegistry;
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.templateRegistry;
    }

    @Override
    protected AbstractTemplatePackagesAggregate getTemplatePackages() {
        return this.templatePackages;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void removeFromCache(final String name, final String versionName, final boolean isworkingCopy) {
        this.moduleRegistry.removeFromCache(new ModuleKey(name, new HesperidesVersion(versionName, isworkingCopy)));
    }

    public void removeAllCache() {
        this.moduleRegistry.removeAllCache();
    }

    @Override
    public void regenerateCache() {
        // TODO
    }
}
