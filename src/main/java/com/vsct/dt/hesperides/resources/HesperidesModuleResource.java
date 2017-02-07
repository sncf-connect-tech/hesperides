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
import com.google.common.collect.ImmutableSet;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.indexation.search.ModuleSearch;
import com.vsct.dt.hesperides.indexation.search.ModuleSearchResponse;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by william_montaz on 02/12/2014.
 */
@Path("/modules")
@Api("/modules")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesModuleResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesModuleResource.class);

    private final Modules      modules;
    private final ModuleSearch moduleSearch;

    public HesperidesModuleResource(final Modules modules, final ModuleSearch moduleSearch) {
        this.modules = modules;
        this.moduleSearch = moduleSearch;
    }

    @GET
    @Timed
    @ApiOperation("Get all module names")
    public Collection<String> getModuleNames(@Auth final User user) {
        return modules.getAllModules().stream().map(Module::getName).collect(Collectors.toSet());
    }

    @Path("/{module_name}")
    @GET
    @Timed
    @ApiOperation("Get all versions for a given module")
    public Collection<String> getModuleVersions(@Auth final User user,
                                                @PathParam("module_name") final String moduleName) {
        checkQueryParameterNotEmpty("module_name", moduleName);

        return modules.getAllModules().stream().filter(e -> e.getName().equals(moduleName)).map(Module::getVersion).collect(Collectors.toSet());
    }

    @Path("/{module_name}/{module_version}")
    @GET
    @Timed
    @ApiOperation("Get all types for a given module version")
    public Collection<String> getModuleTypes(@Auth final User user,
                                             @PathParam("module_name") final String moduleName,
                                             @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        return modules.getAllModules().stream()
                .filter(e -> e.getName().equals(moduleName) && e.getVersion().equals(moduleVersion))
                .map(e -> (e.isWorkingCopy() ? WorkingCopy.LC : Release.LC)).collect(Collectors.toList());
    }

    @Path("/{module_name}/{module_version}/{module_type}")
    @GET
    @Timed
    @ApiOperation("Get info for a given module release/working-copy")
    public Module getModuleInfo(@Auth final User user,
                                @PathParam("module_name") final String moduleName,
                                @PathParam("module_version") final String moduleVersion,
                                @PathParam("module_type") final String moduleType) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("module_type", moduleType);

        final ModuleKey moduleKey;

        if (WorkingCopy.is(moduleType)) {
            moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        } else if (Release.is(moduleType)) {
            moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        } else {
            throw new MissingResourceException(moduleType + " is not a valid module type. Choose either workingcopy or release");
        }

        return modules.getModule(moduleKey).orElseThrow(() -> new MissingResourceException("Could not find module info for " + moduleKey));
    }

    @Path("/perform_search")
    @POST
    @Timed
    @ApiOperation("Get modules by tokens")
    public List<Module> searchPackages(@Auth final User user, @QueryParam("terms") final String tokens) {

        checkQueryParameterNotEmpty("terms", tokens);

        String local_tokens = tokens.toLowerCase();

        String firstTokens = local_tokens;
        String lastToken = "";

        if (local_tokens.lastIndexOf(" ") > -1) {
            firstTokens = local_tokens.substring(0, local_tokens.lastIndexOf(" "));
            lastToken = local_tokens.substring(local_tokens.lastIndexOf(" ") + 1);
        }
//        String[] terms = tokens.split("#");
//        for (int i = 0; i < terms.length; i++) {
//            terms[i] = "*" + terms[i] + "*";
//        }

        String[] terms = new String[2];

        terms[0] = firstTokens;
        terms[1] = lastToken;

        return moduleSearch.getModulesByNameAndVersionLike(terms).stream()
                .map(searchResponse -> {
                    ModuleKey moduleKey = new ModuleKey(
                            searchResponse.getName(),
                            new HesperidesVersion(searchResponse.getVersion(), searchResponse.isWorkingCopy())
                    );

                    Optional<Module> mod = modules.getModule(moduleKey);

                    if (mod.isPresent()) {
                        return modules.getModule(moduleKey).get();
                    } else {
                        return new Module("fake", "1.0", false, ImmutableSet.of(), 0);
                    }
                })
                .collect(Collectors.toList());
    }

    @Path("/search")
    @POST
    @Timed
    @ApiOperation("Get module")
    public Response searchOne(@Auth final User user, @QueryParam("terms") final String tokens) {
        checkQueryParameterNotEmpty("terms", tokens);

        List<ModuleSearchResponse> result = moduleSearch.getModulesByNameAndVersion(tokens.split(" "));

        return result.size() > 0 ? Response.ok(result.get(0)).build(): Response.status(404).build();
    }

    @Path("/{module_name}/{module_version}/workingcopy/templates")
    @GET
    @Timed
    @ApiOperation("Get all templates bundled in a module of a version workingcopy")
    public List<TemplateListItem> getAllTemplatesInWorkingCopy(@Auth final User user,
                                                                 @PathParam("module_name") final String moduleName,
                                                                 @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        return modules.getAllTemplates(moduleKey)
                .stream().map(template -> new TemplateListItem(template))
                .collect(Collectors.toList());
    }

    @Path("/{module_name}/{module_version}/workingcopy")
    @DELETE
    @Timed
    @ApiOperation("Deletes the working copy")
    public Response deleteWorkingCopy(@Auth final User user,
                                      @PathParam("module_name") final String moduleName,
                                      @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        modules.delete(moduleKey);
        return Response.ok().build();
    }

    @Path("/{module_name}/{module_version}/release")
    @DELETE
    @Timed
    @ApiOperation("Deletes the release")
    public Response deleteRelease(@Auth final User user,
                                      @PathParam("module_name") final String moduleName,
                                      @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        modules.delete(moduleKey);
        return Response.ok().build();
    }

    @Path("/{module_name}/{module_version}/release/templates")
    @GET
    @Timed
    @ApiOperation("Get all templates bundled in a module of a version release")
    public List<TemplateListItem> getAllTemplatesInRelease(@Auth final User user,
                                                             @PathParam("module_name") final String moduleName,
                                                             @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        return modules.getAllTemplates(moduleKey)
                .stream().map(template -> new TemplateListItem(template))
                .collect(Collectors.toList());
    }

    @Path("/{module_name}/{module_version}/workingcopy/templates/{template_name}")
    @GET
    @Timed
    @ApiOperation("Get template bundled in a module for a version workingcopy")
    public Template getTemplateInWorkingCopy(@Auth final User user,
                                               @PathParam("module_name") final String moduleName,
                                               @PathParam("module_version") final String moduleVersion,
                                               @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        ModuleKey moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        return modules.getTemplate(moduleKey, templateName).orElseThrow(() -> new MissingResourceException("Could not find template in " + moduleKey + "/" + templateName));
    }

    @Path("/{module_name}/{module_version}/release/templates/{template_name}")
    @GET
    @Timed
    @ApiOperation("Get template bundled in a module for a version release")
    public Template getTemplateInRelease(@Auth final User user,
                                           @PathParam("module_name") final String moduleName,
                                           @PathParam("module_version") final String moduleVersion,
                                           @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        ModuleKey moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        return modules.getTemplate(moduleKey, templateName).orElseThrow(() -> new MissingResourceException("Could not find template " + moduleKey + "/" + templateName));
    }

    @Path("/{module_name}/{module_version}/release/model")
    @GET
    @Timed
    @ApiOperation("Get properties model for a release")
    public HesperidesPropertiesModel getReleaseModel(@Auth final User user,
                                    @PathParam("module_name") final String moduleName,
                                    @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        LOGGER.debug("Get properties model for module release {}/{}", moduleName, moduleVersion);
        ModuleKey moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        return modules.getModel(moduleKey).orElseThrow(() -> new MissingResourceException("Could not find module " + moduleKey));
    }

    @Path("/{module_name}/{module_version}/workingcopy/model")
    @GET
    @Timed
    @ApiOperation("Get properties model for a workingcopy")
    public HesperidesPropertiesModel getWorkingCopyModel(@Auth final User user,
                                        @PathParam("module_name") final String moduleName,
                                        @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        LOGGER.debug("Get properties model for module workingcopy {}/{}", moduleName, moduleVersion);
        ModuleKey moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        return modules.getModel(moduleKey).orElseThrow(() -> new MissingResourceException("Could not find module " + moduleKey));
    }

    @Path("/{module_name}/{module_version}/workingcopy/templates")
    @PUT
    @Timed
    @ApiOperation("Update template in the workingcopy of a module")
    public Template updateTemplateInWorkingCopy(@Auth final User user,
                                                  @PathParam("module_name") final String moduleName,
                                                  @PathParam("module_version") final String moduleVersion,
                                                  @Valid final Template template) {

        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey(moduleName, moduleVersion);
        TemplateData templateData = TemplateData.withTemplateName(template.getName())
                .withFilename(template.getFilename())
                .withLocation(template.getLocation())
                .withContent(template.getContent())
                .withRights(template.getRights())
                .withVersionID(template.getVersionID())
                .build();
        LOGGER.debug("update template workingcopy in {}", moduleKey);
        return modules.updateTemplateInWorkingCopy(moduleKey, templateData);
    }

    @Path("/{module_name}/{module_version}/workingcopy/templates")
    @POST
    @Timed
    @ApiOperation("Create template in the workingcopy of a module")
    public Response createTemplateInWorkingCopy(@Auth final User user,
                                                @PathParam("module_name") final String moduleName,
                                                @PathParam("module_version") final String moduleVersion,
                                                @Valid final Template template) {

        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey(moduleName, moduleVersion);
        TemplateData templateData = TemplateData.withTemplateName(template.getName())
                .withFilename(template.getFilename())
                .withLocation(template.getLocation())
                .withContent(template.getContent())
                .withRights(template.getRights())
                .build();
        LOGGER.debug("create template in the workingcopy of {}", moduleKey);
        Template created = modules.createTemplateInWorkingCopy(moduleKey, templateData);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Timed
    @ApiOperation("Create a working copy (possibly from a release)")
    public Response createWorkingCopy(@Auth final User user,
                                      @QueryParam("from_module_name") final String from_module_name,
                                      @QueryParam("from_module_version") final String from_module_version,
                                      @QueryParam("from_is_working_copy") final Boolean isFromWorkingCopy,
                                      @Valid final Module module) {

        if ((from_module_name == null || StringUtils.isBlank(from_module_name))
                && (from_module_version == null || StringUtils.isBlank(from_module_version))
                && isFromWorkingCopy == null) {
            Module created = modules.createWorkingCopy(module);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } else {
            checkQueryParameterNotEmpty("from_module_name", from_module_name);
            checkQueryParameterNotEmpty("from_module_version", from_module_version);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            ModuleKey fromModuleKey = new ModuleKey(
                    from_module_name,
                    new HesperidesVersion(from_module_version, isFromWorkingCopy)
            );

            ModuleWorkingCopyKey newModuleKey = new ModuleWorkingCopyKey(module.getName(), module.getVersion());
            Module created = modules.createWorkingCopyFrom(newModuleKey, fromModuleKey);
            return Response.status(Response.Status.CREATED).entity(created).build();
        }
    }

    @PUT
    @Timed
    @ApiOperation("Update a module working copy")
    public Module updateWorkingCopy(@Auth final User user, @Valid final Module module) {
        return modules.updateWorkingCopy(module);
    }

    @Path("/{module_name}/{module_version}/workingcopy/templates/{template_name}")
    @DELETE
    @Timed
    @ApiOperation("Delete template in the working copy of a version")
    public void deleteTemplateInWorkingCopy(@Auth final User user,
                                            @PathParam("module_name") final String moduleName,
                                            @PathParam("module_version") final String moduleVersion,
                                            @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey(moduleName, moduleVersion);
        modules.deleteTemplateInWorkingCopy(moduleKey, templateName);
    }

    @Path("/{module_name}/{module_version}/workingcopy")
    @GET
    @Timed
    @ApiOperation("Get a module workingcopy")
    public Module getWorkingCopy(@Auth final User user,
                                   @PathParam("module_name") final String moduleName,
                                   @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, WorkingCopy.of(moduleVersion));
        return modules.getModule(moduleKey).orElseThrow(() -> new MissingResourceException("There is no workingcopy for module " + moduleName + "/" + moduleVersion));
    }

    @Path("/{module_name}/{module_version}/release")
    @GET
    @Timed
    @ApiOperation("Get a module release")
    public Module getRelease(@Auth final User user,
                               @PathParam("module_name") final String moduleName,
                               @PathParam("module_version") final String moduleVersion) {
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleKey moduleKey = new ModuleKey(moduleName, Release.of(moduleVersion));
        return modules.getModule(moduleKey).orElseThrow(() -> new MissingResourceException("There is no release for module " + moduleName + "/" + moduleVersion));
    }

    @Path("/create_release")
    @POST
    @Timed
    @ApiOperation("Create a release from an existing workingcopy")
    public Module createRelease(@Auth final User user,
                                  @QueryParam("module_name") final String moduleName,
                                  @QueryParam("module_version") final String moduleVersion,
                                  @QueryParam("release_version") String releaseVersion) {

        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        if(releaseVersion == null || isNonDisplayedChar(releaseVersion)){
            releaseVersion = moduleVersion;
        }

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey(moduleName, moduleVersion);
        return modules.createRelease(moduleKey, releaseVersion);
    }

}
