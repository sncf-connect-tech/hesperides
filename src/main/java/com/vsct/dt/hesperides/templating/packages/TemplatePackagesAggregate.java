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

package com.vsct.dt.hesperides.templating.packages;

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
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Service used to manage templates as "packs".
 * There is no object representing the pack.
 * Templates belong to the same "TemplatePackage" through namespacing
 * Created by william_montaz on 24/11/2014.
 */
public class TemplatePackagesAggregate extends SingleThreadAggregate implements TemplatePackages {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatePackagesAggregate.class);

    /**
     * Internal structure holding in memory state
     */
    private final TemplateRegistry templateRegistry;
    /**
     * Helper class used to return a template model
     */
    private final Models           models;

    /**
     * Constructor using no UserProvider (used when no loggin was possible)
     * @param eventBus  The {@link com.google.common.eventbus.EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     */
    public TemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore) {
        super("TemplatePackages", eventBus, eventStore);
        this.templateRegistry = new TemplateRegistry();
        this.models = new Models(templateRegistry);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus The {@link com.google.common.eventbus.EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link com.vsct.dt.hesperides.storage.EventStore} used to store events
     * @param userProvider A {@link com.vsct.dt.hesperides.storage.UserProvider} that indicates which user is performing the request
     */
    public TemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore, final UserProvider userProvider) {
        super("TemplatePackages", eventBus, eventStore, userProvider);
        this.templateRegistry = new TemplateRegistry();
        this.models = new Models(templateRegistry);
    }

    /**
     * Helper method used to apply a treatment to all templates
     * @param consumer A {@link java.util.function.Consumer} implementation to perform treatment on a template
     */
    public void withAll(Consumer<Template> consumer) {
        templateRegistry.all().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Get a set containing all template
     * @return an {@link java.util.Set} of {@link Template}s
     */
    public Collection<Template> getAll() {
        return templateRegistry.all();
    }

    /**
     * Get all templates for a template package
     * @param packageKey The key describing the template package
     * @return a {@link java.util.Set} of all templates belonging to the TemplatePackage
     */
    public Set<Template> getAllTemplates(final TemplatePackageKey packageKey) {
        return templateRegistry.getAll(packageKey);
    }

    /**
     * Get a template
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     * @return An {@link java.util.Optional} of a template
     */
    public Optional<Template> getTemplate(final TemplatePackageKey packageKey, final String templateName) {
        return templateRegistry.getTemplate(packageKey.getNamespace(), templateName);
    }

    /**
     * Get a template
     * @param templateNamespace The namespace describing the tempalte package
     * @param templateName The name field of the template
     * @return An {@link java.util.Optional} of a template
     */
    @Deprecated
    public Optional<Template> getTemplate(String templateNamespace, String templateName) {
        return templateRegistry.getTemplate(templateNamespace, templateName);
    }

    /**
     * Adds a new template to a working copy of a template package
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package. Whatever the key it will be transformed to a working copy.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The created {@link com.vsct.dt.hesperides.templating.Template} with its versionID
     */
    public Template createTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final TemplateData templateData) {
        return createTemplate(packageKey, templateData);
    }

    /**
     * Creates a template in a template package (working copy or release)
     * The template content has to be validated
     * Fires a {@link com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent}
     * Will throw a {@link com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException} if the template already exists
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The {@link com.vsct.dt.hesperides.templating.Template} created with its new version id
     */
    private Template createTemplate(final TemplatePackageKey packageKey, final TemplateData templateData) {

        Template.validateContent(templateData.getContent());

        TemplateCreatedEvent templateCreatedEvent = this.tryAtomic(packageKey.getEntityName(), () -> {

            if (templateRegistry.exists(packageKey.getNamespace(), templateData.getName()))
                throw new DuplicateResourceException("Cannot create template " + templateData + " because it already exists");

            Template created = new Template(
                    packageKey.getNamespace(),
                    templateData.getName(),
                    templateData.getFilename(),
                    templateData.getLocation(),
                    templateData.getContent(),
                    templateData.getRights(),
                    1L
            );

            templateRegistry.createOrUpdate(created);

            return new TemplateCreatedEvent(created);

        });

        return templateCreatedEvent.getCreated();
    }

    /**
     * Updates a template in a working copy
     * The content has to be validated first (is it mustache compliant ?)
     * The version id provided must match the actual version id of the template.
     * Fires a {@link com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent}
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exists
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package. Whatever the key it will be transformed to a working copy.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return the updated {@link com.vsct.dt.hesperides.templating.Template} with its new version id
     */
    public Template updateTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final TemplateData templateData) {
        return updateTemplate(packageKey, templateData);
    }

    /**
     * Updates a template in a template package
     * The content has to be validated first (is it mustache compliant ?)
     * The version id provided must match the actual version id of the template.
     * Fires a {@link com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent}
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exists
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The updated {@link com.vsct.dt.hesperides.templating.Template} with its new version id
     */
    private Template updateTemplate(final TemplatePackageKey packageKey, final TemplateData templateData) {

        Template.validateContent(templateData.getContent());

        TemplateUpdatedEvent templateUpdatedEvent = this.tryAtomic(packageKey.getEntityName(), () -> {

            Optional<Template> template = templateRegistry.getTemplate(packageKey.getNamespace(), templateData.getName());

            if(template.isPresent()){

                template.get().tryCompareVersionID(templateData.getVersionID());

                    Template updated = new Template(
                            packageKey.getNamespace(),
                            templateData.getName(),
                            templateData.getFilename(),
                            templateData.getLocation(),
                            templateData.getContent(),
                            templateData.getRights(),
                            templateData.getVersionID() + 1
                    );

                    templateRegistry.createOrUpdate(updated);

                    return new TemplateUpdatedEvent(updated);


            } else {
                throw new MissingResourceException("Cannot update template " + templateData + " because it does not exists");
            }

        });

        return templateUpdatedEvent.getUpdated();
    }

    /**
     * Deletes a template in a working copy
     * Whatever package key is given it will be turned to a working copy
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exist
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     */
    public void deleteTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final String templateName) {
        deleteTemplate(packageKey, templateName);
    }

    /**
     * Deletes a template in a working copy
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exist
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     */
    private void deleteTemplate(final TemplatePackageKey packageKey, final String templateName) {
        this.tryAtomic(packageKey.getEntityName(), () -> {

            Optional<Template> templateOptional = templateRegistry.getTemplate(packageKey.getNamespace(), templateName);

            if(templateOptional.isPresent()){

                Template template = templateOptional.get();
                templateRegistry.delete(template.getNamespace(), template.getName());
                return new TemplateDeletedEvent(template.getNamespace(), template.getName(), template.getVersionID());

            } else {
                throw new MissingResourceException("Impossible to delete template " + templateName + " because it does not exist");
            }

        });
    }

    /**
     * Creates a release from a working copy
     * @param workingCopyKey the {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey} of the working copy to create a release from
     * @return The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the created release
     */
    public TemplatePackageKey createRelease(final TemplatePackageWorkingCopyKey workingCopyKey) {
        TemplatePackageKey releaseInfos = new TemplatePackageKey(
                workingCopyKey.getName(),
                Release.of(workingCopyKey.getVersion().getVersionName())
        );
        return createNewTemplatePackageFrom(releaseInfos, workingCopyKey);
    }

    /**
     * Creates a working copy from another template package (working copy or release)
     * @param workingCopyKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey} of the working copy to create
     * @param fromPackageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the template package to copy from
     * @return The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the created working copy
     */
    public TemplatePackageKey createWorkingCopyFrom(final TemplatePackageWorkingCopyKey workingCopyKey, final TemplatePackageKey fromPackageKey) {
        return createNewTemplatePackageFrom(workingCopyKey, fromPackageKey);
    }

    /**
     * Creates a template package from another template package
     * This function is not really atomic because a template might be change will getting the whole list,
     * It is really not likely to happened
     * Fires as many {@link com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent} as templates existing in the "from" template epackage
     * @param newPackageInfo The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the package to create
     * @param fromPackageInfos The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the package to create from
     * @return The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey} of the new package
     */
    private TemplatePackageKey createNewTemplatePackageFrom(final TemplatePackageKey newPackageInfo, final TemplatePackageKey fromPackageInfos) {
        if (templateRegistry.hasNamespace(newPackageInfo.getNamespace())) {
            throw new DuplicateResourceException("Package " + newPackageInfo + "already exists.");
        }
        Set<Template> fromTemplates = templateRegistry.getAllForNamespace(fromPackageInfos.getNamespace());
        fromTemplates.forEach(template -> {
            TemplateData templateData = TemplateData.withTemplateName(template.getName())
                    .withFilename(template.getFilename())
                    .withLocation(template.getLocation())
                    .withContent(template.getContent())
                    .withRights(template.getRights())
                    .build();
            createTemplate(newPackageInfo, templateData);
        });
        //Package info is immutable, just return it
        return newPackageInfo;
    }

    /**
     * Deletes a complete template package ie. all templates related to it
     * Actually, the event stream is preserved and a {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent} is fired
     * @param packageKey The {@link com.vsct.dt.hesperides.templating.packages.TemplatePackageKey}  to delete
     */
    public void delete(final TemplatePackageKey packageKey){

        TemplatePackageDeletedEvent deletedEvent = this.tryAtomic(packageKey.getEntityName(), () -> {

            Set<Template> templates = templateRegistry.getAll(packageKey);

            if(templates.size() == 0){

                throw new MissingResourceException("There is no template package "+packageKey);

            } else {

                templates.forEach(template -> {
                    templateRegistry.delete(packageKey.getNamespace(), template.getName());
                });
                return new TemplatePackageDeletedEvent(packageKey.getName(), packageKey.getVersionName(), packageKey.isWorkingCopy());

            }

        });

    }

    /**
     * Convenient method to get a template package model from the name, version and type
     * It should be replaced in favor of a method using the key object
     * @param name
     * @param version
     * @param isWorkingCopy
     * @return The {@link com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel} for the given template package key
     */
    @Deprecated
    public HesperidesPropertiesModel getModel(final String name, final String version, final boolean isWorkingCopy) {
        TemplatePackageKey packageKey = new TemplatePackageKey(name, version, isWorkingCopy);
        return models.getPropertiesModel(packageKey.getNamespace());
    }

    /**
     * All template package streams are prefixed with that
     * @return
     */
    @Override
    protected String getStreamPrefix() {
        return "template_package";
    }


    /**
     * Replay methods
     *
     * @param event
     */

    @Subscribe
    public void replayTemplateCreatedEvent(final TemplateCreatedEvent event) {
        try {
            Template template = event.getCreated();
            String[] tokens = template.getNamespace().split("#");

            HesperidesVersion version = new HesperidesVersion(tokens[2], tokens[3].equals("WORKINGCOPY"));
            TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            TemplateData templateData = TemplateData.withTemplateName(template.getName())
                    .withFilename(template.getFilename())
                    .withLocation(template.getLocation())
                    .withContent(template.getContent())
                    .withRights(template.getRights())
                    .build();

            TemplatePackagesAggregate.this.createTemplate(packageKey, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying template created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event) {
        try {
            Template template = event.getUpdated();
            String[] tokens = template.getNamespace().split("#");

            HesperidesVersion version = new HesperidesVersion(tokens[2], tokens[3].equals("WORKINGCOPY"));
            TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            TemplateData templateData = TemplateData.withTemplateName(template.getName())
                    .withFilename(template.getFilename())
                    .withLocation(template.getLocation())
                    .withContent(template.getContent())
                    .withRights(template.getRights())
                    .withVersionID(template.getVersionID() - 1)
                    .build();

            TemplatePackagesAggregate.this.updateTemplate(packageKey, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying tempalte updated event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayTemplateDeletedEvent(final TemplateDeletedEvent event) {
        try {
            String[] tokens = event.getNamespace().split("#");

            HesperidesVersion version = new HesperidesVersion(tokens[2], tokens[3].equals("WORKINGCOPY"));
            TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            TemplatePackagesAggregate.this.deleteTemplate(packageKey, event.getName());
        } catch (Exception e) {
            LOGGER.error("Error while replaying template deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event){
        try{
            HesperidesVersion version = event.isWorkingCopy() ? WorkingCopy.of(event.getPackageVersion()) : Release.of(event.getPackageVersion());
            TemplatePackageKey packageKey = TemplatePackageKey.withName(event.getPackageName()).withVersion(version).build();
            TemplatePackagesAggregate.this.delete(packageKey);
        } catch (Exception e){
            LOGGER.error("Error while replaying template package deleted event {}", e.getMessage());
        }
    }

}
