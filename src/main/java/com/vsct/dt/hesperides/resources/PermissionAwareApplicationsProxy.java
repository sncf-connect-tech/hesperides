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
import org.elasticsearch.common.collect.Sets;

import java.util.Collection;
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

    public PermissionAwareApplicationsProxy(ApplicationsAggregate applicationsAggregate, UserContext userContext) {
        this.applicationsAggregate = applicationsAggregate;
        this.userContext = userContext;
    }

    @Override
    public void withAllPlatforms(Consumer<PlatformData> consumer) {
        //No security for now on this method that is not exposed on resources
        this.applicationsAggregate.withAllPlatforms(consumer);
    }

    @Override
    public Optional<ApplicationData> getApplication(String applicationName) {
        //No security needed to read informations
        return this.applicationsAggregate.getApplication(applicationName);
    }

    @Override
    public Optional<PlatformData> getPlatform(PlatformKey platformKey) {
        //No security needed to read informations
        return this.applicationsAggregate.getPlatform(platformKey);
    }

    @Override
    public Optional<TimeStampedPlatformData> getPlatform(PlatformKey platformKey, long timestamp) {
        //No security needed to read informations
        return this.applicationsAggregate.getPlatform(platformKey, timestamp);
    }

    @Override
    public PlatformData createPlatform(PlatformData platform) {
        if(platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }
        return this.applicationsAggregate.createPlatform(platform);
    }

    @Override
    public PlatformData createPlatformFromExistingPlatform(PlatformData platform, PlatformKey fromPlatformKey) {

        if(platform.isProduction() && !userContext.getCurrentUser().isProdUser()) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }

        Optional<PlatformData> fromPlatform =  this.applicationsAggregate.getPlatform(fromPlatformKey);
        if(fromPlatform.isPresent() && fromPlatform.get().isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Creating a platform from a production platform is reserved to production role");
        }
        return this.applicationsAggregate.createPlatformFromExistingPlatform(platform, fromPlatformKey);
    }

    @Override
    public PlatformData updatePlatform(PlatformData platform, boolean isCopyingPropertiesForUpdatedModules) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(platform.getKey()).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+platform));
        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
        }
        if(platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Upgrading a platform to production is reserved to production role");
        }
        return this.applicationsAggregate.updatePlatform(platform, isCopyingPropertiesForUpdatedModules);
    }

    @Override
    public PropertiesData getProperties(PlatformKey platformKey, String path) {
        //No security needed to read informations
        return this.applicationsAggregate.getProperties(platformKey, path);
    }

    @Override
    public PropertiesData getProperties(PlatformKey platformKey, String path, long timestamp) {
        //No security needed to read informations
        return this.applicationsAggregate.getProperties(platformKey, path, timestamp);
    }

    @Override
    public PropertiesData getSecuredProperties(PlatformKey platformKey, String path, HesperidesPropertiesModel model) {
        PlatformData platform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+ platformKey));
        PropertiesData properties = getProperties(platformKey, path);

        if ( platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            return hideProperties(properties, model);
        }

        return properties;
    }

    @Override
    public PropertiesData getSecuredProperties(PlatformKey platformKey, String path, long timestamp, HesperidesPropertiesModel model) {
        PlatformData platform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+ platformKey));
        PropertiesData properties = getProperties(platformKey, path, timestamp);

        if ( platform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            return hideProperties(properties, model);
        }

        return properties;
    }

    @Override
    public PropertiesData createOrUpdatePropertiesInPlatform(PlatformKey platformKey, String path, PropertiesData properties, long platformVersionID, String comment) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+ platformKey));
        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
        }
        return this.applicationsAggregate.createOrUpdatePropertiesInPlatform(platformKey, path, properties, platformVersionID, comment);
    }

    @Override
    public InstanceModel getInstanceModel(PlatformKey platformKey, String instanceName) {
        //No security needed to read informations
        return this.applicationsAggregate.getInstanceModel(platformKey, instanceName);
    }

    @Override
    public int getAllPlatformsCount() {
        //No security needed to read informations
        return this.applicationsAggregate.getAllPlatformsCount();
    }

    @Override
    public int getAllApplicationsCount() {
        //No security needed to read informations
        return this.applicationsAggregate.getAllApplicationsCount();
    }

    @Override
    public int getAllModulesCount() {
        //No security needed to read informations
        return this.applicationsAggregate.getAllModulesCount();
    }

    @Override
    public Collection<PlatformData> getApplicationsFromSelector(ApplicationSelector selector) {
        // No security needed to read informations
        return this.applicationsAggregate.getApplicationsFromSelector(selector);
    }

    @Override
    public void delete(PlatformKey key) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+key));
        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Deleting a production platform is reserved to production role");
        }
        this.applicationsAggregate.delete(key);
    }

    @Override
    public long takeSnapshot(PlatformKey key) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+key));
        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Snapshoting a production platform is reserved to production role");
        }
        return this.applicationsAggregate.takeSnapshot(key);
    }

    @Override
    public List<Long> getSnapshots(PlatformKey key) {
        return this.applicationsAggregate.getSnapshots(key);
    }

    @Override
    public PlatformData restoreSnapshot(PlatformKey key, long timestamp) {
        PlatformData existingPlatform = this.applicationsAggregate.getPlatform(key).orElseThrow(() -> new MissingResourceException("Cannot check permissions for "+key));
        if(existingPlatform.isProduction() && !userContext.getCurrentUser().isProdUser()){
            throw new ForbiddenOperationException("Restoring snapshot for a production platform is reserved to production role");
        }
        return this.applicationsAggregate.restoreSnapshot(key, timestamp);
    }

    @Override
    public UserContext getUserContext() {
        return this.userContext;
    }

    private PropertiesData hideProperties(PropertiesData properties, HesperidesPropertiesModel model){
        // Hidding password fields for simple properties
        Set<KeyValueValorisationData> keyValueProperties = Sets.newHashSet();
        Set<KeyValuePropertyModel> keyValuePropertyModels = model.getKeyValueProperties();

        properties.getKeyValueProperties().stream().forEach(keyValueValorisation -> {

            // get the model of this property, this is optional because of the global properties has don't have model
            Optional<KeyValuePropertyModel> kvModel = keyValuePropertyModels.stream().filter(keyValuePropertyModel -> keyValuePropertyModel.getName().trim().equals(keyValueValorisation.getName().trim())).findFirst();

            if ( kvModel.isPresent()){
                if ( kvModel.get().isPassword()){
                    keyValueProperties.add(new KeyValueValorisationData(keyValueValorisation.getName(), "********"));
                }else{
                    keyValueProperties.add(keyValueValorisation);
                }
            }else{
                keyValueProperties.add(keyValueValorisation);
            }
        });

        // This is for hidding password on iterable property level 1.

        // Iterable properties multiple level is not yet implemented
        // TODO : Update this when multiple level implemetation is available.

        Set<IterableValorisationData> iterableProperties = Sets.newHashSet();
        Set<IterablePropertyModel> iterablePropertyModels = model.getIterableProperties();

        for (IterableValorisationData iterableValorisationData : properties.getIterableProperties()){

            IterablePropertyModel subModel = iterablePropertyModels.stream().filter(iterablePropertyModel -> iterablePropertyModel.getName().trim().equals(iterableValorisationData.getName().trim())).findFirst().orElseThrow(() -> new MissingResourceException(String.format("No model found for the property '%s'. You probably have some whitespaces on it", iterableValorisationData.getName())));

            // Checking the item data
            List<IterableValorisationData.IterableValorisationItemData> iterableValorisationItemDatas = Lists.newArrayList();
            for (IterableValorisationData.IterableValorisationItemData iterableValorisationItemData : iterableValorisationData.getIterableValorisationItems()){

                // Checking the valorisations
                Set<ValorisationData> values = Sets.newHashSet();
                for (ValorisationData valorisationData : iterableValorisationItemData.getValues() ){
                    Property itModel = subModel.getFields().stream().filter(property -> property.getName().trim().equals(valorisationData.getName().trim())).findFirst().orElseThrow(() -> new MissingResourceException(String.format("No model found for the property '%s'. You probably have some whitespaces on it", valorisationData.getName())));
                    if ( itModel.isPassword() ){
                        values.add(new KeyValueValorisationData(valorisationData.getName(), "********"));
                    }else{
                        values.add(valorisationData);
                    }
                }
                iterableValorisationItemDatas.add(new IterableValorisationData.IterableValorisationItemData(iterableValorisationItemData.getTitle(),values));
            }

            iterableProperties.add(new IterableValorisationData(iterableValorisationData.getName(), iterableValorisationItemDatas));
        }

        // Return the hidden properties
        return new PropertiesData(keyValueProperties, iterableProperties);
    }
}
