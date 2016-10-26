package com.vsct.dt.hesperides.templating.modules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.storage.SingleThreadAggregate;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.cache.ModuleStoragePrefixInterface;
import com.vsct.dt.hesperides.templating.modules.event.*;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.AbstractTemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service used to manage modules and related templates.
 * Templates are loosely tied to modules (only via the namespace attributes).
 * This helps evolving the model defining how templates are tied together.
 * Created by william_montaz on 02/12/2014.
 */
public abstract class AbstractModulesAggregate extends SingleThreadAggregate
        implements Modules, ModuleEventInterface, ModuleStoragePrefixInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModulesAggregate.class);

    /**
     * Constructor using no particular UserProvider (used when there was no authentication)
     * @param eventBus {@link EventBus} used to propagate events
     * @param eventStore {@link EventStore} used to store events
     */
    public AbstractModulesAggregate(final EventBus eventBus, final EventStore eventStore) {
        super("Modules", eventBus, eventStore);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus {@link EventBus} used to propagate events
     * @param eventStore {@link EventStore} used to store events
     * @param userProvider {@link UserProvider} used to get information about current user
     */
    public AbstractModulesAggregate(final EventBus eventBus, final EventStore eventStore,
                                    final UserProvider userProvider) {
        super("Modules", eventBus, eventStore, userProvider);
    }


    /**
     * Convenient method used to apply actions on all modules (ex. Reindexation)
     * @param consumer {@link Consumer} performing actions on a single module
     */
    public void withAll(Consumer<Module> consumer) {
        getModuleRegistry().getAllModules().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Convenient method used to apply actions on all templates. Templates are loosely tied to modules
     * @param consumer {@link Consumer} performing actions on a single template
     */
    public void withAllTemplates(Consumer<Template> consumer) {
        getTemplateRegistry().allTemplates().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Get a set containing all modules (moduels are unique)
     * @return an {@link Set} of {@link Module}s
     */
    @Override
    public Collection<Module> getAllModules() {
        return getModuleRegistry().getAllModules();
    }

    /**
     * Get a set containing all template
     * @return an {@link java.util.Set} of {@link Template}s
     */
    public Collection<Template> getAll() {
        return getTemplateRegistry().allTemplates();
    }

    /**
     * Get immutable representation of a module
     * @param moduleKey {@link ModuleKey} describing module
     * @return an {@link Optional} of a {@link Module}
     */
    public Optional<Module> getModule(final ModuleKey moduleKey) {
        return getModuleRegistry().getModule(moduleKey);
    }

    /**
     * Get all templates related to a module
     * @param moduleKey {@link ModuleKey} describing module
     * @return {@link List} of {@link Template}
     */
    public List<Template> getAllTemplates(final ModuleKey moduleKey) {
        return getTemplateRegistry().getAllTemplates(moduleKey).stream()
                .collect(Collectors.toList());
    }

    /**
     * Get a specific template in a module
     * @param moduleKey {@link ModuleKey} describing module
     * @param templateName Name of the template
     * @return an {@link Optional} of a {@link Template}
     */
    public Optional<Template> getTemplate(final ModuleKey moduleKey, String templateName) {
        return getTemplateRegistry().getTemplate(moduleKey.getNamespace(), templateName);
    }

    /**
     * Returns the properties model of a module
     * It combines the model related to module specific tempaltes and the models related to technos
     * @param moduleKey @link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing module
     * @return The {@link HesperidesPropertiesModel}
     */
    public Optional<HesperidesPropertiesModel> getModel(final ModuleKey moduleKey) {
        if(! getModuleRegistry().existsModule(moduleKey)) return Optional.empty();

        HesperidesPropertiesModel packageModel = getModels().getPropertiesModel(moduleKey.getNamespace());

        final ImmutableSet<Techno> technos = this.getModule(moduleKey)
                .map(module -> ImmutableSet.copyOf(module.getTechnos()))
                .orElse(ImmutableSet.<Techno>of());

        for (final Techno techno : technos) {
            final HesperidesPropertiesModel model = getTemplatePackages().getModel(techno.getName(), techno.getVersion(), techno.isWorkingCopy());
            packageModel = packageModel.merge(model);
        }

        return Optional.of(packageModel);
    }

    /**
     * Creates a working copy of a module
     * @param moduleSource {@link Module} representing the module
     * @return the {@link Module} created with its version ID
     */
    public Module createWorkingCopy(final Module moduleSource) {
        //Make sure it is working copy
        final ModuleKey wcKey = new ModuleKey(moduleSource.getKey().getName(), WorkingCopy.of(moduleSource.getKey().getVersion()));
        return createModule(wcKey, moduleSource, Sets.newHashSet());
    }

    /**
     * Updates a working copy of the module.
     * This method only updates the module object and not the templates related thus it is not possible to update fields representing the key of the module
     * @param module {@link Module} represneting the module
     * @return The updated {@link Module} with its new version ID
     */
    public Module updateWorkingCopy(final Module module) {
        //Make sure to search the working copy
        final ModuleKey wcInfo = new ModuleKey(module.getKey().getName(), WorkingCopy.of(module.getKey().getVersion()));

        final HesperidesCommand<ModuleWorkingCopyUpdatedEvent> hc = new ModuleWorkingCopyUpdatedCommand(getModuleRegistry(),
                wcInfo, module);

        final ModuleWorkingCopyUpdatedEvent workingCopyUpdatedEvent = this.tryAtomic(wcInfo.getEntityName(), hc);

        return workingCopyUpdatedEvent.getUpdated();
    }

    /**
     * Updates a template in a module working copy
     * Version id provided in tempalte data has to be the same as the actual version id of the template
     * NB: Separation of module version id and template version id is questionnable as it does not really provide optimistic locking on the module object itself
     * Since it doesn't lead to any problems right now, no use to change it
     * @param moduleKey {@link ModuleWorkingCopyKey} describing the module working copy
     * @param templateData Might need to change for TemplateVO
     * @return {@link Template} with its new version ID
     */
    public Template updateTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final TemplateData templateData) {
        Template.validateContent(templateData.getContent());

        final ModuleTemplateUpdatedCommand hc = new ModuleTemplateUpdatedCommand(getTemplateRegistry(), moduleKey,
                templateData);

        final ModuleTemplateUpdatedEvent moduleTemplateUpdatedEvent = this.tryAtomic(moduleKey.getEntityName(), hc);

        return moduleTemplateUpdatedEvent.getUpdated();

    }

    /**
     * Creates a tempalte in a module working copy
     * @param moduleKey {@link ModuleWorkingCopyKey} describing the module working copy
     * @param templateData Might need to change for TemplateVO
     * @return {@link Template} with its new version ID
     */
    public Template createTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final TemplateData templateData) {

        final ModuleKey wcInfo = new ModuleKey(moduleKey.getName(), WorkingCopy.of(moduleKey.getVersion()));

        Template.validateContent(templateData.getContent());

        final ModuleTemplateCreatedCommand hc = new ModuleTemplateCreatedCommand(getTemplateRegistry(), wcInfo, templateData);

        final ModuleTemplateCreatedEvent moduleTemplateCreatedEvent = this.tryAtomic(wcInfo.getEntityName(), hc);

        return moduleTemplateCreatedEvent.getCreated();
    }

    /**
     * Removes a template in a module working copy
     * @param moduleKey {@link ModuleWorkingCopyKey} describing the module working copy
     * @param templateName
     */
    public void deleteTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final String templateName) {
        final ModuleKey wcInfo = new ModuleKey(moduleKey.getName(), WorkingCopy.of(moduleKey.getVersion()));

        final ModuleTemplateDeletedCommand hc = new ModuleTemplateDeletedCommand(getTemplateRegistry(), wcInfo,
                templateName);

        this.tryAtomic(wcInfo.getEntityName(), hc);
    }

    /**
     * Creates a working copy of a module from another module
     * Templates are also copied from the source module
     * @param newModuleKey {@link ModuleWorkingCopyKey} describing the module working copy
     * @param fromModuleKey {@link ModuleKey} describing the module to copy from
     * @return
     */
    public Module createWorkingCopyFrom(final ModuleWorkingCopyKey newModuleKey, final ModuleKey fromModuleKey) {
        return createModuleFrom(newModuleKey, fromModuleKey);
    }

    /**
     * Creates a release from an existing working copy
     * Templates are copied
     * @param fromModuleKey {@link ModuleWorkingCopyKey} describing the module working copy to create the release from
     * @param releaseVersion The version of the release. This can be useful to transform a 1.0-SNAPSHOT working copy to a 1.0 release
     * @return
     */
    public Module createRelease(final ModuleWorkingCopyKey fromModuleKey, final String releaseVersion) {
        final ModuleKey releaseKey = new ModuleKey(fromModuleKey.getName(), Release.of(releaseVersion));
        return createModuleFrom(releaseKey, fromModuleKey);
    }

    /**
     * Create a module from another module
     * The operation is not really atomic since the module and the related templates could have been changed while the method execute
     * It is not likely to happened due to the low charge of hesperides
     * @param newModuleKey
     * @param fromModuleKey
     * @return the freshly created module
     */
    private Module createModuleFrom(final ModuleKey newModuleKey, final ModuleKey fromModuleKey) {
        final Module fromModule = getModuleRegistry().getModule(fromModuleKey).orElseThrow(
                () -> new MissingResourceException("There is no module " + fromModuleKey + " to build " + newModuleKey)
        );

        //Get templates
        final Set<Template> templatesFrom = getTemplateRegistry().getAllTemplatesForNamespace(fromModuleKey.getNamespace());

        //Be sure to create the event
        return this.createModule(newModuleKey, fromModule, templatesFrom);
    }

    /**
     * Creates a new module with a set of templates
     * @param newModuleKey
     * @param moduleSource
     * @param templatesFrom Set of {@link Template} to add to the module created
     * @return the freshly created module
     */
    private Module createModule(final ModuleKey newModuleKey, final Module moduleSource,
                                final Set<Template> templatesFrom) {

        final ModuleCreatedCommand hc = new ModuleCreatedCommand(getModuleRegistry(), getTemplateRegistry(), newModuleKey,
            moduleSource, templatesFrom);

        final ModuleCreatedEvent moduleCreatedEvent = this.tryAtomic(newModuleKey.getEntityName(), hc);

        return moduleCreatedEvent.getModuleCreated();
    }

    /**
     * Deletes a module and all related templates
     * The module underlying stream is not actually deleted, just an event is posted
     * @param moduleKey {@link ModuleKey} to delete
     */
    public void delete(final ModuleKey moduleKey){

        final ModuleDeletedCommand hc = new ModuleDeletedCommand(getModuleRegistry(), getTemplateRegistry(), moduleKey);

        this.tryAtomic(moduleKey.getEntityName(), hc);
    }

    /*

    REPLAY METHODS

     */


    @Subscribe
    public void replayModuleCreatedEvent(final ModuleCreatedEvent event) {
        try {
            final Module module = event.getModuleCreated();
            final ModuleKey key = module.getKey();
            this.createModule(key, module, event.getTemplates());
        } catch (Exception e) {
            LOGGER.error("Error while replaying module created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleWorkingCopyUpdatedEvent(final ModuleWorkingCopyUpdatedEvent event) {
        try {
            final Module module = event.getUpdated();
            final Module withDecrementedVersionId = new Module(module.getKey(), module.getTechnos(), module.getVersionID() - 1);
            this.updateWorkingCopy(withDecrementedVersionId);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module working copy updated event {}", e.getMessage());
        }
    }


    @Subscribe
    public void replayModuleTemplateCreatedEvent(final ModuleTemplateCreatedEvent event) {
        try {
            final ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());

            TemplateData templateData = TemplateData.withTemplateName(event.getCreated().getName())
                    .withFilename(event.getCreated().getFilename())
                    .withLocation(event.getCreated().getLocation())
                    .withContent(event.getCreated().getContent())
                    .withRights(event.getCreated().getRights())
                    .build();

            this.createTemplateInWorkingCopy(key, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleTemplateUpdatedEvent(final ModuleTemplateUpdatedEvent event) {
        try {
            final ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());

            final TemplateData templateData = TemplateData.withTemplateName(event.getUpdated().getName())
                    .withFilename(event.getUpdated().getFilename())
                    .withLocation(event.getUpdated().getLocation())
                    .withContent(event.getUpdated().getContent())
                    .withRights(event.getUpdated().getRights())
                    .withVersionID(event.getUpdated().getVersionID() - 1)
                    .build();

            this.updateTemplateInWorkingCopy(key, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template updated event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleTemplateDeletedEvent(final ModuleTemplateDeletedEvent event) {
        try {
            final ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());
            this.deleteTemplateInWorkingCopy(key, event.getTemplateName());
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleDeletedEvent(final ModuleDeletedEvent event) {
        try {
            final HesperidesVersion version = event.isWorkingCopy() ? WorkingCopy.of(event.getModuleVersion()) : Release.of(event.getModuleVersion());
            final ModuleKey moduleKey = new ModuleKey(event.getModuleName(), version);
            this.delete(moduleKey);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module deleted event {}", e.getMessage());
        }
    }

    protected abstract ModuleRegistryInterface getModuleRegistry();

    protected abstract TemplateRegistryInterface getTemplateRegistry();

    protected abstract AbstractTemplatePackagesAggregate getTemplatePackages();

    protected abstract Models getModels();

    @Override
    public Integer getAllTechnosCount() {
        return null;
    }
}
