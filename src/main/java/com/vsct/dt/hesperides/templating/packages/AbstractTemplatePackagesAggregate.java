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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.SingleThreadAggregate;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.cache.TemplatePackageStoragePrefixInterface;
import com.vsct.dt.hesperides.templating.packages.event.*;
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
public abstract class AbstractTemplatePackagesAggregate extends SingleThreadAggregate
        implements TemplatePackages, TemplatePackageEventInterface, TemplatePackageStoragePrefixInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTemplatePackagesAggregate.class);

    /**
     * Constructor using no UserProvider (used when no loggin was possible)
     * @param eventBus  The {@link EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link EventStore} used to store events
     */
    public AbstractTemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore) {
        super("TemplatePackages", eventBus, eventStore);
    }

    /**
     * Constructor using a specific UserProvider
     * @param eventBus The {@link EventBus} used to propagate events to other part of the application
     * @param eventStore The {@link EventStore} used to store events
     * @param userProvider A {@link UserProvider} that indicates which user is performing the request
     */
    public AbstractTemplatePackagesAggregate(final EventBus eventBus, final EventStore eventStore,
                                             final UserProvider userProvider) {
        super("TemplatePackages", eventBus, eventStore, userProvider);
    }

    /**
     * Helper method used to apply a treatment to all templates
     * @param consumer A {@link Consumer} implementation to perform treatment on a template
     */
    @Override
    public void withAll(Consumer<Template> consumer) {
        getTemplateRegistry().allTemplates().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }

    /**
     * Get all templates for a template package
     * @param packageKey The key describing the template package
     * @return a {@link Set} of all templates belonging to the TemplatePackage
     */
    @Override
    public Set<Template> getAllTemplates(final TemplatePackageKey packageKey) {
        return getTemplateRegistry().getAllTemplates(packageKey);
    }

    @Override
    public Collection<Template> getAllTemplates() {
        return getTemplateRegistry().allTemplates();
    }

    /**
     * Get a template
     * @param packageKey The {@link TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     * @return An {@link Optional} of a template
     */
    @Override
    public Optional<Template> getTemplate(final TemplatePackageKey packageKey, final String templateName) {
        return getTemplateRegistry().getTemplate(packageKey.getNamespace(), templateName);
    }

    /**
     * Get a template
     * @param templateNamespace The namespace describing the tempalte package
     * @param templateName The name field of the template
     * @return An {@link Optional} of a template
     */
    @Deprecated
    @Override
    public Optional<Template> getTemplate(String templateNamespace, String templateName) {
        return getTemplateRegistry().getTemplate(templateNamespace, templateName);
    }

    /**
     * Adds a new template to a working copy of a template package
     * @param packageKey The {@link TemplatePackageKey} describing the template package. Whatever the key it will be transformed to a working copy.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The created {@link Template} with its versionID
     */
    @Override
    public Template createTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final TemplateData templateData) {
        return createTemplate(packageKey, templateData);
    }

    /**
     * Creates a template in a template package (working copy or release)
     * The template content has to be validated
     * Fires a {@link TemplateCreatedEvent}
     * Will throw a {@link DuplicateResourceException} if the template already exists
     * @param packageKey The {@link TemplatePackageKey} describing the template package
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The {@link Template} created with its new version id
     */
    private Template createTemplate(final TemplatePackageKey packageKey, final TemplateData templateData) {

        Template.validateContent(templateData.getContent());

        final TemplateCreatedCommand hc = new TemplateCreatedCommand(getTemplateRegistry(), packageKey, templateData);

        final TemplateCreatedEvent templateCreatedEvent = this.tryAtomic(packageKey.getEntityName(), hc);

        return templateCreatedEvent.getCreated();
    }

    /**
     * Updates a template in a working copy
     * The content has to be validated first (is it mustache compliant ?)
     * The version id provided must match the actual version id of the template.
     * Fires a {@link TemplateUpdatedEvent}
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exists
     * @param packageKey The {@link TemplatePackageKey} describing the template package. Whatever the key it will be transformed to a working copy.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return the updated {@link Template} with its new version id
     */
    @Override
    public Template updateTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final TemplateData templateData) {
        return updateTemplate(packageKey, templateData);
    }

    /**
     * Updates a template in a template package
     * The content has to be validated first (is it mustache compliant ?)
     * The version id provided must match the actual version id of the template.
     * Fires a {@link TemplateUpdatedEvent}
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exists
     * @param packageKey The {@link TemplatePackageKey} describing the template package.
     * @param templateData The data held by the template -> should be replaced by templateVO
     * @return The updated {@link Template} with its new version id
     */
    private Template updateTemplate(final TemplatePackageKey packageKey, final TemplateData templateData) {

        Template.validateContent(templateData.getContent());

        final TemplateUpdatedCommand hc = new TemplateUpdatedCommand(getTemplateRegistry(), packageKey, templateData);

        final TemplateUpdatedEvent templateUpdatedEvent = this.tryAtomic(packageKey.getEntityName(), hc);

        return templateUpdatedEvent.getUpdated();
    }

    /**
     * Deletes a template in a working copy
     * Whatever package key is given it will be turned to a working copy
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exist
     * @param packageKey The {@link TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     */
    @Override
    public void deleteTemplateInWorkingCopy(final TemplatePackageWorkingCopyKey packageKey, final String templateName) {
        deleteTemplate(packageKey, templateName);
    }

    /**
     * Deletes a template in a working copy
     * Will throw {@link com.vsct.dt.hesperides.exception.runtime.MissingResourceException} if the template does not exist
     * @param packageKey The {@link TemplatePackageKey} describing the template package
     * @param templateName The name field of the template
     */
    private void deleteTemplate(final TemplatePackageKey packageKey, final String templateName) {
        final TemplateDeletedCommand hc = new TemplateDeletedCommand(getTemplateRegistry(), packageKey, templateName);

        this.tryAtomic(packageKey.getEntityName(), hc);
    }

    /**
     * Creates a release from a working copy
     * @param workingCopyKey the {@link TemplatePackageWorkingCopyKey} of the working copy to create a release from
     * @return The {@link TemplatePackageKey} of the created release
     */
    @Override
    public TemplatePackageKey createRelease(final TemplatePackageWorkingCopyKey workingCopyKey) {
        final TemplatePackageKey releaseInfos = new TemplatePackageKey(
                workingCopyKey.getName(),
                Release.of(workingCopyKey.getVersion().getVersionName())
        );
        return createNewTemplatePackageFrom(releaseInfos, workingCopyKey);
    }

    /**
     * Creates a working copy from another template package (working copy or release)
     * @param workingCopyKey The {@link TemplatePackageWorkingCopyKey} of the working copy to create
     * @param fromPackageKey The {@link TemplatePackageKey} of the template package to copy from
     * @return The {@link TemplatePackageKey} of the created working copy
     */
    @Override
    public TemplatePackageKey createWorkingCopyFrom(final TemplatePackageWorkingCopyKey workingCopyKey, final TemplatePackageKey fromPackageKey) {
        return createNewTemplatePackageFrom(workingCopyKey, fromPackageKey);
    }

    /**
     * Creates a template package from another template package
     * This function is not really atomic because a template might be change will getting the whole list,
     * It is really not likely to happened
     * Fires as many {@link TemplateCreatedEvent} as templates existing in the "from" template epackage
     * @param newPackageInfo The {@link TemplatePackageKey} of the package to create
     * @param fromPackageInfos The {@link TemplatePackageKey} of the package to create from
     * @return The {@link TemplatePackageKey} of the new package
     */
    private TemplatePackageKey createNewTemplatePackageFrom(final TemplatePackageKey newPackageInfo, final TemplatePackageKey fromPackageInfos) {
        if (getTemplateRegistry().templateHasNamespace(newPackageInfo.getNamespace())) {
            throw new DuplicateResourceException("Package " + newPackageInfo + "already exists.");
        }

        final Set<Template> fromTemplates = getTemplateRegistry().getAllTemplatesForNamespace(fromPackageInfos.getNamespace());

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
     * Actually, the event stream is preserved and a {@link TemplatePackageDeletedEvent} is fired
     * @param packageKey The {@link TemplatePackageKey}  to delete
     */
    @Override
    public void delete(final TemplatePackageKey packageKey){
        final TemplatePackageDeletedCommand hc = new TemplatePackageDeletedCommand(getTemplateRegistry(), packageKey);

        this.tryAtomic(packageKey.getEntityName(), hc);

    }

    /**
     * Convenient method to get a template package model from the name, version and type
     * It should be replaced in favor of a method using the key object
     * @param name
     * @param version
     * @param isWorkingCopy
     * @return The {@link HesperidesPropertiesModel} for the given template package key
     */
    @Deprecated
    @Override
    public HesperidesPropertiesModel getModel(final String name, final String version, final boolean isWorkingCopy) {
        final TemplatePackageKey packageKey = new TemplatePackageKey(name, version, isWorkingCopy);
        return getModels().getPropertiesModel(packageKey.getNamespace());
    }

    @Subscribe
    @Override
    public void replayTemplateCreatedEvent(final TemplateCreatedEvent event) {
        try {
            final Template template = event.getCreated();
            final String[] tokens = template.getNamespace().split("#");

            final HesperidesVersion version = new HesperidesVersion(tokens[2], WorkingCopy.is(tokens[3]));
            final TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            final TemplateData templateData = TemplateData.withTemplateName(template.getName())
                    .withFilename(template.getFilename())
                    .withLocation(template.getLocation())
                    .withContent(template.getContent())
                    .withRights(template.getRights())
                    .build();

            createTemplate(packageKey, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying template created event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event) {
        try {
            final Template template = event.getUpdated();
            final String[] tokens = template.getNamespace().split("#");

            final HesperidesVersion version = new HesperidesVersion(tokens[2], WorkingCopy.is(tokens[3]));
            final TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            final TemplateData templateData = TemplateData.withTemplateName(template.getName())
                    .withFilename(template.getFilename())
                    .withLocation(template.getLocation())
                    .withContent(template.getContent())
                    .withRights(template.getRights())
                    .withVersionID(template.getVersionID() - 1)
                    .build();

            this.updateTemplate(packageKey, templateData);
        } catch (Exception e) {
            LOGGER.error("Error while replaying tempalte updated event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replayTemplateDeletedEvent(final TemplateDeletedEvent event) {
        try {
            String[] tokens = event.getNamespace().split("#");

            final HesperidesVersion version = new HesperidesVersion(tokens[2], WorkingCopy.is(tokens[3]));
            final TemplatePackageKey packageKey = new TemplatePackageKey(tokens[1], version);

            AbstractTemplatePackagesAggregate.this.deleteTemplate(packageKey, event.getName());
        } catch (Exception e) {
            LOGGER.error("Error while replaying template deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event){
        try{
            final HesperidesVersion version = event.isWorkingCopy() ? WorkingCopy.of(event.getPackageVersion()) : Release.of(event.getPackageVersion());
            final TemplatePackageKey packageKey = TemplatePackageKey.withName(event.getPackageName()).withVersion(version).build();
            AbstractTemplatePackagesAggregate.this.delete(packageKey);
        } catch (Exception e){
            LOGGER.error("Error while replaying template package deleted event {}", e.getMessage());
        }
    }

    /**
     * Internal structure holding in memory state.
     *
     * @return template registry
     */
    protected abstract TemplateRegistryInterface getTemplateRegistry();

    /**
     * Helper class used to return a template model.
     *
     * @return model
     */
    protected abstract Models getModels();
}
