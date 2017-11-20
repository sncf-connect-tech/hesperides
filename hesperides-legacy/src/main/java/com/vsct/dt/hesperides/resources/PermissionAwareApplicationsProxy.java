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

package com.vsct.dt.hesperides.resources;

import com.google.common.collect.Lists;
import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.exception.runtime.ForbiddenOperationException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.IterablePropertyModel;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.models.Property;
import com.vsct.dt.hesperides.templating.platform.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 27/02/2015.
 */
public class PermissionAwareApplicationsProxy implements Applications  {


    private final ApplicationsAggregate applicationsAggregate;
    private final UserContext           userContext;

    public PermissionAwareApplicationsProxy(final ApplicationsAggregate applicationsAggregate, final UserContext userContext) {
        this.applicationsAggregate = applicationsAggregate;
        this.userContext = userContext;
    }

    @Override
    public Optional<PlatformData> getPlatform(final PlatformKey platformKey) {
        //No security needed to read informations
        return this.applicationsAggregate.getPlatform(platformKey);
    }

    @Override
    public Optional<TimeStampedPlatformData> getPlatform(final PlatformKey platformKey, final long timestamp) {
        //No security needed to read informations
        return this.applicationsAggregate.getPlatform(platformKey, timestamp);
    }

    @Override
    public PlatformData createPlatform(final PlatformData platform) {
        if (platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }

        return this.applicationsAggregate.createPlatform(platform);
    }

    @Override
    public PlatformData createPlatformFromExistingPlatform(final PlatformData platform, final PlatformKey fromPlatformKey) {

        if (platform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }

        final Optional<PlatformData> fromPlatform =  this.applicationsAggregate.getPlatform(fromPlatformKey);

        if (fromPlatform.isPresent() && fromPlatform.get().isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Creating a platform from a production platform is reserved to production role");
        }

        return this.applicationsAggregate.createPlatformFromExistingPlatform(platform, fromPlatformKey);
    }

    @Override
    public PlatformData updatePlatform(final PlatformData platform, final boolean isCopyingPropertiesForUpdatedModules) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(platform.getKey()).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+platform));

        if (existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
        }

        if (platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Upgrading a platform to production is reserved to production role");
        }

        return this.applicationsAggregate.updatePlatform(platform, isCopyingPropertiesForUpdatedModules);
    }

    @Override
    public PropertiesData getProperties(final PlatformKey platformKey, final String path) {
        //No security needed to read informations
        return this.applicationsAggregate.getProperties(platformKey, path);
    }

    @Override
    public PropertiesData getProperties(final PlatformKey platformKey, final String path, final long timestamp) {
        //No security needed to read informations
        return this.applicationsAggregate.getProperties(platformKey, path, timestamp);
    }

    @Override
    public PropertiesData getSecuredProperties(final PlatformKey platformKey, final String path, final HesperidesPropertiesModel model) {
        final PlatformData platform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Cannot " +
                "check permissions for "+ platformKey));
        final PropertiesData properties = getProperties(platformKey, path);

        if (platform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            return hideProperties(properties, model);
        }

        return properties;
    }

    @Override
    public PropertiesData getSecuredProperties(final PlatformKey platformKey, final String path, final long timestamp,
            final HesperidesPropertiesModel model) {
        final PlatformData platform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Cannot " +
                "check permissions for "+ platformKey));
        final PropertiesData properties = getProperties(platformKey, path, timestamp);

        if (platform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            return hideProperties(properties, model);
        }

        return properties;
    }

    @Override
    public PropertiesData createOrUpdatePropertiesInPlatform(final PlatformKey platformKey, final String path,
            final PropertiesData properties, final long platformVersionID, final String comment) {
        final PlatformData existingPlatform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException
                ("Cannot check permissions for "+ platformKey));

        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
        }

        return this.applicationsAggregate.createOrUpdatePropertiesInPlatform(platformKey, path, properties, platformVersionID, comment);
    }

    @Override
    public InstanceModel getInstanceModel(final PlatformKey platformKey, final String instanceName) {
        //No security needed to read informations
        return this.applicationsAggregate.getInstanceModel(platformKey, instanceName);
    }

    @Override
    public Collection<PlatformData> getAllPlatforms() {
        return this.applicationsAggregate.getAllPlatforms();
    }

    @Override
    public void delete(final PlatformKey key) {
        final PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot " +
                "check permissions for "+key));

        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Deleting a production platform is reserved to production role");
        }
        this.applicationsAggregate.delete(key);
    }

    @Override
    public long takeSnapshot(final PlatformKey key) {
        final PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot " +
                "check permissions for "+key));

        if (existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            throw new ForbiddenOperationException("Snapshoting a production platform is reserved to production role");
        }

        return this.applicationsAggregate.takeSnapshot(key);
    }

    @Override
    public List<Long> getSnapshots(final PlatformKey key) {
        return this.applicationsAggregate.getSnapshots(key);
    }

    @Override
    public PlatformData restoreSnapshot(final PlatformKey key, final long timestamp) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+key));

        if (existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            throw new ForbiddenOperationException("Restoring snapshot for a production platform is reserved to production role");
        }

        return this.applicationsAggregate.restoreSnapshot(key, timestamp);
    }

    /**
     * Hides password field in valuations.
     *
     * @param valorisations : the valuations
     * @param model : the model of thes valuations
     * @return the valuations with hidden password fields
     */
    private Set<ValorisationData> hideValorisations(final Set<ValorisationData> valorisations, final Set<Property> model) {
        final Set<ValorisationData> hidden = new HashSet<>();

        for (ValorisationData val : valorisations) {
            Optional<Property> optModel = model.stream()
                    .filter(property -> property.getName().trim().equals(val.getName().trim()))
                    .findFirst();

            if (optModel.isPresent()) {
                final Property _model = optModel.get();

                if (val instanceof KeyValueValorisationData) {
                    final KeyValueValorisationData _val = (KeyValueValorisationData) val;

                    if (_model.isPassword()) {
                        hidden.add( new KeyValueValorisationData(_val.getName(), "********"));
                    } else {
                        hidden.add( new KeyValueValorisationData(_val.getName(), _val.getValue()));
                    }
                } else {
                    // iterable
                    hidden.add( hideIterableValorisation((IterableValorisationData) val, (IterablePropertyModel) _model));
                }
            }
        }

        return hidden;
    }

    /**
     * Hides password fields of a iterable item
     * @param item : the item
     * @param model : the model of the item
     * @return the item with hidden fields
     */
    private IterableValorisationData.IterableValorisationItemData hideItem(
            final IterableValorisationData.IterableValorisationItemData item, final IterablePropertyModel model) {
        final Set<ValorisationData> values = hideValorisations(item.getValues(), model.getFields());

        final IterableValorisationData.IterableValorisationItemData hiddenItem
                = new IterableValorisationData.IterableValorisationItemData(item.getTitle(), values);

        return hiddenItem;
    }

    /**
     * Hides password fields of an iterable valuation
     * @param iterable : the iterable property
     * @param model : the model of the property
     * @return the iterable property with hidden fields
     */
    private IterableValorisationData hideIterableValorisation (final IterableValorisationData iterable,
            final IterablePropertyModel model) {
        final List<IterableValorisationData.IterableValorisationItemData> items = Lists.newArrayList();

        for (IterableValorisationData.IterableValorisationItemData item : iterable.getIterableValorisationItems()) {
            items.add(hideItem(item, model));
        }

        final IterableValorisationData hidden = new IterableValorisationData(iterable.getName(), items);

        return hidden;
    }

    /**
     * Hiddes password fields of properties
     *
     * @param properties : the properties
     * @param model : the model
     * @return the properties with password fields
     */
    private PropertiesData hideProperties(final PropertiesData properties, final HesperidesPropertiesModel model) {
        // 1 - Hidding password fields for simple properties
        final Set<KeyValueValorisationData> keyValueProperties = new HashSet<>();

        final Set<KeyValuePropertyModel> keyValuePropertyModels = model.getKeyValueProperties();

        properties.getKeyValueProperties().stream().forEach(keyValueValorisation -> {

            // get the model of this property, this is optional because of the global properties has don't have model
            final Optional<KeyValuePropertyModel> kvModel = keyValuePropertyModels.stream().filter(keyValuePropertyModel -> keyValuePropertyModel
                    .getName().trim().equals(keyValueValorisation.getName().trim())).findFirst();

            if (kvModel.isPresent()){
                if (kvModel.get().isPassword()) {
                    keyValueProperties.add(new KeyValueValorisationData(keyValueValorisation.getName(), "********"));
                } else {
                    keyValueProperties.add(keyValueValorisation);
                }
            } else {
                keyValueProperties.add(keyValueValorisation);
            }
        });

        // 2 - Hidding password for iterable properties
        final Set<IterableValorisationData> iterableProperties = new HashSet<>();

        final Set<IterablePropertyModel> iterablePropertyModels = model.getIterableProperties();

        for (ValorisationData val : properties.getIterableProperties()) {
            final Optional<IterablePropertyModel> subModel = iterablePropertyModels.stream().filter(iterablePropertyModel -> iterablePropertyModel
                    .getName().trim().equals(val.getName().trim())).findFirst();

            if (subModel.isPresent()) {
                iterableProperties.add(hideIterableValorisation((IterableValorisationData) val, subModel.get()));
            }
        }

        // 3 - Return the hidden properties
        return new PropertiesData(keyValueProperties, iterableProperties);
    }
}
