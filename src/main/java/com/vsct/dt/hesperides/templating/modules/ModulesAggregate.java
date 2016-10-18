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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.SingleThreadAggregate;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.TemplateData;
import com.vsct.dt.hesperides.templating.TemplateRegistry;
import com.vsct.dt.hesperides.templating.TemplateSlurper;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service used to manage modules and related templates.
 * Templates are loosely tied to modules (only via the namespace attributes).
 * This helps evolving the model defining how templates are tied together.
 * Created by william_montaz on 02/12/2014.
 */
public class ModulesAggregate extends SingleThreadAggregate implements Modules {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesAggregate.class);

    /**
     * Helper class to generate properties model from templates
     */
    private final Models models;

    /**
     * Inner class storing modules (InMemory state)
     */
    private final ModuleRegistry moduleRegistry;

    /**
     * Inner class storing tempaltes related to modules (InMemory State)
     */
    private final TemplateRegistry templateRegistry;

    /**
     * TemplatePackage aggregate used to retrieve templates associated to a "techno"
     */
    private final TemplatePackagesAggregate templatePackages;

    /**
     * Constructor using no particular UserProvider (used when there was no authentication)
     * @param eventBus {@link com.google.common.eventbus.EventBus} used to propagate events
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param templatePackages {@link com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate} used to get techno related tempaltes
     */
    public ModulesAggregate(final EventBus eventBus, final EventStore eventStore, final TemplatePackagesAggregate templatePackages) {
        super("Modules", eventBus, eventStore);
        this.templatePackages = templatePackages;
        this.moduleRegistry = new ModuleRegistry();
        this.templateRegistry = new TemplateRegistry();
        this.models = new Models(templateRegistry);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus {@link com.google.common.eventbus.EventBus} used to propagate events
     * @param eventStore {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param templatePackages {@link com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate} used to get techno related tempaltes
     * @param userProvider {@link com.vsct.dt.hesperides.storage.UserProvider} used to get information about current user
     */
    public ModulesAggregate(final EventBus eventBus, final EventStore eventStore, final TemplatePackagesAggregate templatePackages, final UserProvider userProvider) {
        super("Modules", eventBus, eventStore, userProvider);
        this.templatePackages = templatePackages;
        this.moduleRegistry = new ModuleRegistry();
        this.templateRegistry = new TemplateRegistry();
        this.models = new Models(templateRegistry);
    }

    /**
     * Convenient method used to apply actions on all modules (ex. Reindexation)
     * @param consumer {@link java.util.function.Consumer} performing actions on a single module
     */
    public void withAll(Consumer<Module> consumer) {
        moduleRegistry.getAllModules().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Get a set containing all technos name
     *
     * @return an {@link java.util.Set} of {@link Techno}s
     */
    @Override
    public Integer getAllTechnosCount() {

        //All modules
        Collection<Module> modules = getAllModules();

        final Collection<String> allOfTheTechnos = new HashSet<String>();

        modules.stream().forEach(module -> {
            module.getTechnos().stream().forEach(techno -> {
                allOfTheTechnos.add(techno.getName());
            });
        });

        return allOfTheTechnos.size();
    }

    /**
     * Convenient method used to apply actions on all templates. Templates are loosely tied to modules
     * @param consumer {@link java.util.function.Consumer} performing actions on a single template
     */
    public void withAllTemplates(Consumer<Template> consumer) {
        templateRegistry.all().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Get a set containing all modules (moduels are unique)
     * @return an {@link java.util.Set} of {@link Module}s
     */
    @Override
    public Collection<Module> getAllModules() {
        return moduleRegistry.getAllModules();
    }

    /**
     * Get immutable representation of a module
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing module
     * @return an {@link java.util.Optional} of a {@link Module}
     */
    public Optional<Module> getModule(final ModuleKey moduleKey) {
        return moduleRegistry.getModule(moduleKey);
    }

    /**
     * Get all templates related to a module
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing module
     * @return {@link java.util.List} of {@link com.vsct.dt.hesperides.templating.Template}
     */
    public List<Template> getAllTemplates(final ModuleKey moduleKey) {
        return templateRegistry.getAll(moduleKey).stream()
                .collect(Collectors.toList());
    }

    /**
     * Get a specific template in a module
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing module
     * @param templateName Name of the template
     * @return an {@link java.util.Optional} of a {@link com.vsct.dt.hesperides.templating.Template}
     */
    public Optional<Template> getTemplate(final ModuleKey moduleKey, String templateName) {
        return templateRegistry.getTemplate(moduleKey.getNamespace(), templateName);
    }

    /**
     * Returns the properties model of a module
     * It combines the model related to module specific tempaltes and the models related to technos
     * @param moduleKey @link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing module
     * @return The {@link com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel}
     */
    public Optional<HesperidesPropertiesModel> getModel(final ModuleKey moduleKey) {
        if(! moduleRegistry.exists(moduleKey)) return Optional.empty();

        HesperidesPropertiesModel packageModel = models.getPropertiesModel(moduleKey.getNamespace());

        Set<Techno> technos = this.getModule(moduleKey)
                .map(module -> module.getTechnos())
                .orElse(ImmutableSet.<Techno>of());


        for (final Techno techno : technos) {
            HesperidesPropertiesModel model = templatePackages.getModel(techno.getName(), techno.getVersion(), techno.isWorkingCopy());
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
        ModuleKey wcKey = new ModuleKey(moduleSource.getKey().getName(), WorkingCopy.of(moduleSource.getKey().getVersion()));
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
        ModuleKey wcInfo = new ModuleKey(module.getKey().getName(), WorkingCopy.of(module.getKey().getVersion()));

        ModuleWorkingCopyUpdatedEvent workingCopyUpdatedEvent = this.tryAtomic(wcInfo.getEntityName(), () -> {

            Optional<Module> moduleOptional = moduleRegistry.getModule(wcInfo);

            if(moduleOptional.isPresent()) {

                moduleOptional.get().tryCompareVersionID(module.getVersionID());
                    Module updatedModule = new Module(wcInfo, module.getTechnos(), module.getVersionID() + 1);
                    moduleRegistry.createOrUpdate(updatedModule);
                    return new ModuleWorkingCopyUpdatedEvent(updatedModule);

            } else {
                throw new MissingResourceException("Cannot update because module working copy " + wcInfo + " does not exists");
            }

        });

        return workingCopyUpdatedEvent.getUpdated();
    }

    /**
     * Updates a template in a module working copy
     * Version id provided in tempalte data has to be the same as the actual version id of the template
     * NB: Separation of module version id and template version id is questionnable as it does not really provide optimistic locking on the module object itself
     * Since it doesn't lead to any problems right now, no use to change it
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey} describing the module working copy
     * @param templateData Might need to change for TemplateVO
     * @return {@link com.vsct.dt.hesperides.templating.Template} with its new version ID
     */
    public Template updateTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final TemplateData templateData) {
        Template.validateContent(templateData.getContent());

        ModuleTemplateUpdatedEvent moduleTemplateUpdatedEvent = this.tryAtomic(moduleKey.getEntityName(), () -> {

            Optional<Template> templateOptional = templateRegistry.getTemplate(
                    moduleKey.getNamespace(),
                    templateData.getName());

            if(templateOptional.isPresent()){

                templateOptional.get().tryCompareVersionID(templateData.getVersionID());

                    Template updated = new Template(
                            moduleKey.getNamespace(),
                            templateData.getName(),
                            templateData.getFilename(),
                            templateData.getLocation(),
                            templateData.getContent(),
                            templateData.getRights(),
                            templateData.getVersionID() + 1
                    );

                    templateRegistry.createOrUpdate(updated);

                    return new ModuleTemplateUpdatedEvent(moduleKey.getName(), moduleKey.getVersionName(), updated);

            } else {
                throw new MissingResourceException("Cannot update template " + templateData + " because it does not exists");
            }


        });

        return moduleTemplateUpdatedEvent.getUpdated();

    }

    /**
     * Creates a tempalte in a module working copy
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey} describing the module working copy
     * @param templateData Might need to change for TemplateVO
     * @return {@link com.vsct.dt.hesperides.templating.Template} with its new version ID
     */
    public Template createTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final TemplateData templateData) {

        ModuleKey wcInfo = new ModuleKey(moduleKey.getName(), WorkingCopy.of(moduleKey.getVersion()));

        Template.validateContent(templateData.getContent());

        ModuleTemplateCreatedEvent moduleTemplateCreatedEvent = this.tryAtomic(wcInfo.getEntityName(), () -> {

            if (templateRegistry.exists(wcInfo.getNamespace(), templateData.getName()))
                throw new DuplicateResourceException("Cannot create template " + templateData + " because it already exists");

            Template created = new Template(
                    wcInfo.getNamespace(),
                    templateData.getName(),
                    templateData.getFilename(),
                    templateData.getLocation(),
                    templateData.getContent(),
                    templateData.getRights(),
                    1L
            );

            templateRegistry.createOrUpdate(created);

            return new ModuleTemplateCreatedEvent(wcInfo.getName(), wcInfo.getVersion().getVersionName(), created);

        });

        return moduleTemplateCreatedEvent.getCreated();
    }

    /**
     * Removes a template in a module working copy
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey} describing the module working copy
     * @param templateName
     */
    public void deleteTemplateInWorkingCopy(final ModuleWorkingCopyKey moduleKey, final String templateName) {
        ModuleKey wcInfo = new ModuleKey(moduleKey.getName(), WorkingCopy.of(moduleKey.getVersion()));

        this.tryAtomic(wcInfo.getEntityName(), () -> {

            Optional<Template> optionalTemplate = templateRegistry.getTemplate(wcInfo.getNamespace(), templateName);

            if(optionalTemplate.isPresent()){
                Template templateToDelete = optionalTemplate.get();
                templateRegistry.delete(templateToDelete.getNamespace(), templateToDelete.getName());
                return new ModuleTemplateDeletedEvent(wcInfo.getName(), wcInfo.getVersion().getVersionName(), templateName);
            } else {
                throw new MissingResourceException("Impossible to delete template " + templateName + " because it does not exist");
            }

        });
    }

    /**
     * Creates a working copy of a module from another module
     * Templates are also copied from the source module
     * @param newModuleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey} describing the module working copy
     * @param fromModuleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleKey} describing the module to copy from
     * @return
     */
    public Module createWorkingCopyFrom(final ModuleWorkingCopyKey newModuleKey, final ModuleKey fromModuleKey) {
        return createModuleFrom(newModuleKey, fromModuleKey);
    }

    /**
     * Creates a release from an existing working copy
     * Templates are copied
     * @param fromModuleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey} describing the module working copy to create the release from
     * @param releaseVersion The version of the release. This can be useful to transform a 1.0-SNAPSHOT working copy to a 1.0 release
     * @return
     */
    public Module createRelease(final ModuleWorkingCopyKey fromModuleKey, final String releaseVersion) {
        ModuleKey releaseKey = new ModuleKey(fromModuleKey.getName(), Release.of(releaseVersion));
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
        Module fromModule = moduleRegistry.getModule(fromModuleKey).orElseThrow(
                () -> new MissingResourceException("There is no module " + fromModuleKey + " to build " + newModuleKey)
        );

        //Get templates
        Set<Template> templatesFrom = templateRegistry.getAllForNamespace(fromModuleKey.getNamespace());

        //Be sure to create the event
        return this.createModule(newModuleKey, fromModule, templatesFrom);
    }

    /**
     * Creates a new module with a set of templates
     * @param newModuleKey
     * @param moduleSource
     * @param templatesFrom Set of {@link com.vsct.dt.hesperides.templating.Template} to add to the module created
     * @return the freshly created module
     */
    private Module createModule(final ModuleKey newModuleKey, final Module moduleSource, final Set<Template> templatesFrom) {
        ModuleCreatedEvent moduleCreatedEvent = this.tryAtomic(newModuleKey.getEntityName(), () -> {

            if (moduleRegistry.exists(newModuleKey)) {
                throw new DuplicateResourceException("Module " + newModuleKey + " already exists");
            }

            Module module = new Module(newModuleKey, moduleSource.getTechnos(), 1L);
            moduleRegistry.createOrUpdate(module);

            Set<Template> newTemplates = templatesFrom.stream().map(template -> {
                Template newTemplate = new Template(
                        newModuleKey.getNamespace(),
                        template.getName(),
                        template.getFilename(),
                        template.getLocation(),
                        template.getContent(),
                        template.getRights(),
                        1L
                );
                templateRegistry.createOrUpdate(newTemplate);
                return newTemplate;
            }).collect(Collectors.toSet());

            return new ModuleCreatedEvent(module, newTemplates);

        });

        return moduleCreatedEvent.getModuleCreated();
    }

    /**
     * Deletes a module and all related templates
     * The module underlying stream is not actually deleted, just an event is posted
     * @param moduleKey {@link com.vsct.dt.hesperides.templating.modules.ModuleKey} to delete
     */
    public void delete(final ModuleKey moduleKey){

        ModuleDeletedEvent moduleDeletedEvent = this.tryAtomic(moduleKey.getEntityName(), () -> {

            Optional<Module> moduleOptional = moduleRegistry.getModule(moduleKey);

            if(moduleOptional.isPresent()) {

                //Delete all templates
                templateRegistry.getAll(moduleKey).forEach(template -> {
                    templateRegistry.delete(moduleKey.getNamespace(), template.getName());
                });

                //Delete the module
                moduleRegistry.delete(moduleKey);

                return new ModuleDeletedEvent(moduleKey.getName(), moduleKey.getVersionName(), moduleKey.isWorkingCopy());

            } else {

                throw new MissingResourceException(moduleKey+" does not exist");
            }


        });

    }

    /**
     * Helper method to validate tempalte content, this should be rmeoved as it has been moved
     * @param content
     */
    private void validateContent(final String content) {
        TemplateSlurper templateSlurper = new TemplateSlurper(content);
        /* This is a way to validate the template */
        templateSlurper.generatePropertiesScope();
    }

    @Override
    protected String getStreamPrefix() {
        return "module";
    }

    /*

    REPLAY METHODS

     */


    @Subscribe
    public void replayModuleCreatedEvent(final ModuleCreatedEvent event) {
        try {
            Module module = event.getModuleCreated();
            ModuleKey key = module.getKey();
            ModulesAggregate.this.createModule(key, module, event.getTemplates());
        } catch (Exception e) {
            LOGGER.error("Error while replaying module created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleWorkingCopyUpdatedEvent(final ModuleWorkingCopyUpdatedEvent event) {
        try {
            Module module = event.getUpdated();
            Module withDecrementedVersionId = new Module(module.getKey(), module.getTechnos(), module.getVersionID() - 1);
            ModulesAggregate.this.updateWorkingCopy(withDecrementedVersionId);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module working copy updated event {}", e.getMessage());
        }
    }


    @Subscribe
    public void replayModuleTemplateCreatedEvent(final ModuleTemplateCreatedEvent event) {
        try {
            ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());

            TemplateData templateData = TemplateData.withTemplateName(event.getCreated().getName())
                    .withFilename(event.getCreated().getFilename())
                    .withLocation(event.getCreated().getLocation())
                    .withContent(event.getCreated().getContent())
                    .withRights(event.getCreated().getRights())
                    .build();

            ModulesAggregate.this.createTemplateInWorkingCopy(key, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleTemplateUpdatedEvent(final ModuleTemplateUpdatedEvent event) {
        try {
            ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());

            TemplateData templateData = TemplateData.withTemplateName(event.getUpdated().getName())
                    .withFilename(event.getUpdated().getFilename())
                    .withLocation(event.getUpdated().getLocation())
                    .withContent(event.getUpdated().getContent())
                    .withRights(event.getUpdated().getRights())
                    .withVersionID(event.getUpdated().getVersionID() - 1)
                    .build();

            ModulesAggregate.this.updateTemplateInWorkingCopy(key, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template updated event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleTemplateDeletedEvent(final ModuleTemplateDeletedEvent event) {
        try {
            ModuleWorkingCopyKey key = new ModuleWorkingCopyKey(event.getModuleName(), event.getModuleVersion());
            ModulesAggregate.this.deleteTemplateInWorkingCopy(key, event.getTemplateName());
        } catch (Exception e) {
            LOGGER.error("Error while replaying module template deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayModuleDeletedEvent(final ModuleDeletedEvent event) {
        try {
            HesperidesVersion version = event.isWorkingCopy() ? WorkingCopy.of(event.getModuleVersion()) : Release.of(event.getModuleVersion());
            ModuleKey moduleKey = new ModuleKey(event.getModuleName(), version);
            ModulesAggregate.this.delete(moduleKey);
        } catch (Exception e) {
            LOGGER.error("Error while replaying module deleted event {}", e.getMessage());
        }
    }

}
