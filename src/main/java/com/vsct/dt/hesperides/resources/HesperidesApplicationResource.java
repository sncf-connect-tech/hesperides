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

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.applications.InstanceModel;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.indexation.search.ApplicationSearch;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.templating.platform.ApplicationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.TimeStampedPlatformData;
import com.vsct.dt.hesperides.util.HesperidesUtil;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import com.vsct.dt.hesperides.util.converter.ApplicationConverter;
import com.vsct.dt.hesperides.util.converter.PlatformConverter;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;
import com.vsct.dt.hesperides.util.converter.TimeStampedPlatformConverter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.stream.Collectors;

import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by william_montaz on 11/07/14.
 *
 * Modified by tidiane_sidibe on 05/08/2016 : adding the comment to event
 */
@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api("/applications")
public final class HesperidesApplicationResource extends BaseResource {

    private final Applications applications;
    private final Modules modules;
    private final ApplicationSearch applicationSearch;
    private final PlatformConverter platformConverter;
    private final PropertiesConverter propertiesConverter;
    final TimeStampedPlatformConverter timeStampedPlatformConverter;
    private ResponseConverter<PlatformData, Platform> responsePlatformConverter;
    private ResponseConverter<TimeStampedPlatformData, TimeStampedPlatform> responseTimeConverter;
    private ResponseConverter<ApplicationData, Application> responseApplicationConverter;

    private final HesperidesModuleResource moduleResource;

    public HesperidesApplicationResource(final Applications applications, final Modules modules,
                                         final ApplicationSearch applicationSearch,
                                         final TimeStampedPlatformConverter timeStampedPlatformConverter,
                                         final ApplicationConverter applicationDataConverter,
                                         final PropertiesConverter propertiesConverter,
                                         final HesperidesModuleResource moduleResource) {
        this.applications = applications;
        this.modules = modules;
        this.applicationSearch = applicationSearch;
        this.platformConverter = timeStampedPlatformConverter.getPlatformConverter();
        this.timeStampedPlatformConverter = timeStampedPlatformConverter;
        this.responsePlatformConverter = obj -> platformConverter.toPlatform(obj);
        this.responseTimeConverter = obj -> timeStampedPlatformConverter.toTimeStampedPlatform(obj);
        this.responseApplicationConverter = obj -> applicationDataConverter.toApplication(obj);
        this.propertiesConverter = propertiesConverter;

        this.moduleResource = moduleResource;
    }

    @Path("/using_module/{module}/{version}/{type}")
    @GET
    @Timed
    @ApiOperation("Get application using module")
    public Response getApplicationUsingModule(@Auth final User user, @PathParam("module") final String module, @PathParam("version") final String version,
                                              @PathParam("type") final String type) {

        return Response.ok(applicationSearch.getAllPlatformsUsingModules(module, version, type)).build();
    }

    @Path("/{application_name}/platforms/{platform_name}/global_properties_usage")
    @GET
    @Timed
    @ApiOperation("Retrieve global properties usage in application")
    public Response getGlobalPropertiesUsage(@Auth final User user, @PathParam("application_name") final String applicationName,
                                             @PathParam("platform_name") final String platformName) {

        final Optional<PlatformData> searchPlatform = this.applications.getPlatform(new PlatformKey(applicationName, platformName));

        if (searchPlatform.isPresent()) {
            final PlatformData ptf = searchPlatform.get();

            final Map<String, Set<Map>> usage = new HashMap<>();

            applications
                    .getProperties(new PlatformKey(applicationName, platformName), "#")
                    .getKeyValueProperties()
                    .stream()
                    .forEach(globalProp -> {

                        final Set<Map> buffer = new HashSet<>();

                        ptf.getModules().stream().forEach(elem -> {

                            final PlatformKey currentPlatformKey = new PlatformKey(applicationName, platformName);

                            Optional<HesperidesPropertiesModel> oModel;
                            HesperidesPropertiesModel model;

                            if (elem.isWorkingCopy()) {
                                oModel = modules.getModel(new ModuleKey(elem.getName(), WorkingCopy.of(elem.getVersion())));
                            } else {
                                oModel = modules.getModel(new ModuleKey(elem.getName(), Release.of(elem.getVersion())));
                            }

                            if (oModel.isPresent()) {

                                model = oModel.get();
                                if (model.getKeyValueProperties().stream().anyMatch(prop ->
                                        prop.getName().equals(globalProp.getName()))) {

                                    final Map b = new HashMap();
                                    b.put("path", elem.getPropertiesPath());
                                    b.put("inModel", true);

                                    buffer.add(b);
                                }
                            } else {

                                model = null;
                            }

                            applications
                                    .getProperties(currentPlatformKey, elem.getPropertiesPath())
                                    .getKeyValueProperties()
                                    .stream()
                                    .forEach(prop -> {

                                        if (prop.getName().equals(globalProp.getName())
                                                || prop.getValue().contains("{{" + globalProp.getName() + "}}")) {

                                            if (model == null) {
                                                final Map b = new HashMap();
                                                b.put("path", elem.getPropertiesPath());
                                                b.put("inModel", false);

                                                buffer.add(b);
                                            } else {

                                                final boolean inModel = model.getKeyValueProperties().stream().anyMatch(mod ->
                                                        prop.getName().equals(mod.getName()));

                                                final Map b = new HashMap();
                                                b.put("path", elem.getPropertiesPath());
                                                b.put("inModel", inModel);

                                                buffer.add(b);
                                            }
                                        }
                                    });
                        });

                        usage.put(globalProp.getName(), buffer);
                    });

            return Response.ok(usage).build();
        } else {
            return Response.status(Status.NOT_FOUND).entity("Module application and platform not found").build();
        }
    }

    @Path("/{application_name}")
    @GET
    @Timed
    @ApiOperation("Get application with given name")
    public Response getApplication(@Auth final User user, @PathParam("application_name") final String name) {
        checkQueryParameterNotEmpty("application_name", name);

        return entityWithConverterOrNotFound(applications.getApplication(name), responseApplicationConverter);
    }

    @Path("/perform_search")
    @POST
    @Timed
    @ApiOperation("Get application by name")
    public Set<ApplicationListItem> searchApplications(@Auth final User user, @QueryParam("name") final String name) {
        checkQueryParameterNotEmpty("name", name);

        return applicationSearch.getApplicationsLike(name).stream().map(applicationSearchResponse ->
                        new ApplicationListItem(applicationSearchResponse.getName())
        ).collect(Collectors.toSet());
    }

    @Path("/platforms/perform_search")
    @POST
    @Timed
    @ApiOperation("Get all platform for a given application name")
    public Set<PlatformListItem> searchPlatforms(@Auth final User user, @QueryParam("applicationName") final String applicationName, @QueryParam("platformName") final String platformName) {
        checkQueryParameterNotEmpty("applicationName", applicationName);

        return applicationSearch.getAllPlatforms(applicationName, platformName).stream().map(platformSearchResponse ->
                        new PlatformListItem(platformSearchResponse.getName())
        ).collect(Collectors.toSet());
    }

    @Path("/{application_name}/platforms/{platform_name}")
    @GET
    @Timed
    @ApiOperation("Get platform for application with given name")
    public Response getPlatform(@Auth final User user,
                                @PathParam("application_name") final String applicationName,
                                @PathParam("platform_name") final String platformName,
                                @QueryParam("timestamp") final Long timestamp) {
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        if(timestamp != null){
            return entityWithConverterOrNotFound(applications.getPlatform(platformKey, timestamp), responseTimeConverter);
        } else {
            return entityWithConverterOrNotFound(applications.getPlatform(platformKey), responsePlatformConverter);
        }
    }

    @Path("/{application_name}/platforms/{platform_name}")
    @DELETE
    @Timed
    @ApiOperation("Deletes a platform on a given app")
    public Response deletePlatform(@Auth final User user,
                                   @PathParam("application_name") final String applicationName,
                                   @PathParam("platform_name") final String platformName){
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();
        applications.delete(platformKey);
        return Response.ok().build();
    }

    @Path("/{application_name}/platforms")
    @POST
    @Timed
    @ApiOperation("Create platform for application with given name, possibly from an existing platform")
    public Platform createPlatform(@Auth final User user,
                                     @PathParam("application_name") final String applicationName,
                                     @QueryParam("from_application") final String fromApplication,
                                     @QueryParam("from_platform") final String fromPlatform,
                                     @Valid final Platform platform) {

        if((fromApplication == null || isNonDisplayedChar(fromApplication))
                && (fromPlatform == null || isNonDisplayedChar(fromPlatform))) {
            return platformConverter.toPlatform(applications.createPlatform(platformConverter.toPlatformData(platform)));
        } else {
            PlatformKey fromPlatformKey = PlatformKey.withName(fromPlatform)
                    .withApplicationName(fromApplication)
                    .build();

            checkQueryParameterNotEmpty("from_application", fromApplication);
            checkQueryParameterNotEmpty("from_platform", fromPlatform);

            return platformConverter.toPlatform(
                    applications.createPlatformFromExistingPlatform(
                            platformConverter.toPlatformData(platform),
                            fromPlatformKey));
        }
    }

    @Path("/{application_name}/platforms")
    @PUT
    @Timed
    @ApiOperation("Update platform for application with given name")
    public Platform updatePlatform(@Auth final User user,
                                        @PathParam("application_name") final String applicationName,
                                        @QueryParam("copyPropertiesForUpgradedModules") final boolean copyPropertiesForUpgradedModules,
                                        @Valid final Platform platform) {
        return platformConverter.toPlatform(
                applications.updatePlatform(
                        platformConverter.toPlatformData(platform), copyPropertiesForUpgradedModules));
    }

    @Path("/{application_name}/platforms/{platform_name}/properties")
    @GET
    @Timed
    @ApiOperation("Get properties with the given path in a platform")
    public Properties getProperties(@Auth final User user,
                                      @PathParam("application_name") final String applicationName,
                                      @PathParam("platform_name") final String platformName,
                                      @QueryParam("path") final String path,
                                      @QueryParam("timestamp") final Long timestamp) {
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("path", path);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(), Sets.newHashSet());

        if (path.length() > 1) {
            HesperidesUtil.ModuleInfo info = HesperidesUtil.moduleInfoFromPath(path);
            model = info.isRelease() ? this.moduleResource.getReleaseModel(user, info.getName(), info.getVersion()) : this.moduleResource.getWorkingCopyModel(user, info.getName(), info.getVersion());
        }

        return (timestamp != null) ? propertiesConverter.toProperties(applications.getSecuredProperties(platformKey, path, timestamp, model)) : propertiesConverter.toProperties(applications.getSecuredProperties(platformKey, path, model));
    }

    @Path("/{application_name}/platforms/{platform_name}/properties/instance_model")
    @GET
    @Timed
    @ApiOperation("Get properties with the given path in a platform")
    public InstanceModel getInstanceModel(@Auth final User user,
                                          @PathParam("application_name") final String applicationName,
                                          @PathParam("platform_name") final String platformName,
                                          @QueryParam("path") final String path) {
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("path", path);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();
        return applications.getInstanceModel(platformKey, path);
    }

    @Path("/{application_name}/platforms/{platform_name}/properties")
    @POST
    @Timed
    @ApiOperation("Save properties in a platform with the given path")
    public Properties saveProperties(@Auth final User user,
                                       @PathParam("application_name") final String applicationName,
                                       @PathParam("platform_name") final String platformName,
                                       @QueryParam("path") final String path,
                                       @QueryParam("platform_vid") final Long platformID, //Using Long wrapper object helps preventing Java Primitive Default value of 0
                                       @QueryParam("comment") final String comment,
                                       @Valid final Properties properties) {
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("path", path);
        checkQueryParameterNotEmpty("platform_vid", platformID);
        checkQueryParameterNotEmpty("comment", comment);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        //Filter properties to remove null or empty valuations
        Properties propertiesCleaned;
        if (path.equals("#")) {
            propertiesCleaned = properties;
        } else {
            propertiesCleaned = properties.makeCopyWithoutNullOrEmptyValorisations();
        }

        return propertiesConverter.toProperties(
                applications.createOrUpdatePropertiesInPlatform(platformKey, path,
                        propertiesConverter.toPropertiesData(propertiesCleaned), platformID, comment));
    }

    @Path("/{application_name}/platforms/{platform_name}/take_snapshot")
    @POST
    @Timed
    @ApiOperation("Take a snapshot of the platform")
    public Response takeSnapshot(@Auth final User user,
                                 @PathParam("application_name") final String applicationName,
                                 @PathParam("platform_name") final String platformName){
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        applications.takeSnapshot(platformKey);

        return Response.ok().build();
    }

    @Path("/{application_name}/platforms/{platform_name}/restore_snapshot")
    @POST
    @Timed
    @ApiOperation("Take a snapshot of the platform")
    public Platform restaureSnapshot(@Auth final User user,
                                 @PathParam("application_name") final String applicationName,
                                 @PathParam("platform_name") final String platformName,
                                 @QueryParam("timestamp") final long timestamp){

        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("timestamp", timestamp);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        return platformConverter.toPlatform(applications.restoreSnapshot(platformKey, timestamp));
    }

    @Path("/{application_name}/platforms/{platform_name}/snapshots")
    @GET
    @Timed
    @ApiOperation("Get the list of timestamps corresponding to snapshots of the platform")
    public List<Long> getSnapshots(@Auth final User user,
                                 @PathParam("application_name") final String applicationName,
                                 @PathParam("platform_name") final String platformName){
        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        List<Long> snapshots = applications.getSnapshots(platformKey);
        Collections.sort(snapshots, (longA, longB) -> longB.compareTo(longA));
        return snapshots;
    }
}
