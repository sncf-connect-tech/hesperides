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

package com.vsct.dt.hesperides.cache;

import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.exception.runtime.ForbiddenOperationException;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * Created by emeric_martineau on 13/06/2016.
 */
@Path("/cache")
@Api("/cache")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesCacheResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesCacheResource.class);

    private TemplatePackagesAggregate templatePackagesAggregate;
    private ModulesAggregate modulesAggregate;
    private ApplicationsAggregate applicationsAggregate;

    public HesperidesCacheResource(final TemplatePackagesAggregate templatePackagesAggregate,
                                   final ModulesAggregate modulesAggregate,
                                   final ApplicationsAggregate applicationsAggregate) {
        this.templatePackagesAggregate = templatePackagesAggregate;
        this.modulesAggregate = modulesAggregate;
        this.applicationsAggregate = applicationsAggregate;
    }

    @DELETE
    @Path("/application/{application_name}/{platform_name}")
    @Timed
    @ApiOperation("Remove an application from cache")
    public Response clearApplicationCache(@Auth final User user,
                                          @PathParam("application_name") final String applicationName,
                                          @PathParam("platform_name") final String platformName) {
        LOGGER.info("Remove application {}/{} from memory cache by {}.", applicationName, platformName,
                user.getUsername());

        this.applicationsAggregate.removeFromCache(applicationName, platformName);

        return Response.ok().build();
    }

    @DELETE
    @Path("/applications")
    @Timed
    @ApiOperation("Remove all applications from cache")
    public Response clearApplicationsCache(@Auth final User user) {
        if (!user.isTechUser()) {
            throw new ForbiddenOperationException("Only tech user can clear all applications cache.");
        }

        LOGGER.info("Remove all application from memory cache by {}.", user.getUsername());

        this.applicationsAggregate.removeAllCache();

        return Response.ok().build();
    }

    @DELETE
    @Path("/module/{module_name}/{module_version}/workingcopy")
    @Timed
    @ApiOperation("Remove a workingcopy module from cache")
    public Response clearModuleWorkingcopyCache(@Auth final User user,
                                                @PathParam("module_name") final String moduleName,
                                                @PathParam("module_version") final String moduleVersion) {
        LOGGER.info("Remove module in workingcopy {}/{} from memory cache by {}.", moduleName, moduleVersion,
                user.getUsername());

        this.modulesAggregate.removeFromCache(moduleName, moduleVersion, true);

        return Response.ok().build();
    }

    @DELETE
    @Path("/modules")
    @Timed
    @ApiOperation("Remove all modules from cache")
    public Response clearModulesCache(@Auth final User user) {
        if (!user.isTechUser()) {
            throw new ForbiddenOperationException("Only tech user can clear all modules cache.");
        }

        LOGGER.info("Remove all modules from memory cache by {}.", user.getUsername());

        this.modulesAggregate.removeAllCache();

        return Response.ok().build();
    }

    @DELETE
    @Path("/module/{module_name}/{module_version}/release")
    @Timed
    @ApiOperation("Remove a released module from cache")
    public Response clearModuleReleaseCache(@Auth final User user,
                                                @PathParam("module_name") final String moduleName,
                                                @PathParam("module_version") final String moduleVersion) {
        LOGGER.info("Remove module in release {}/{} from memory cache.", moduleName, moduleVersion);

        this.modulesAggregate.removeFromCache(moduleName, moduleVersion, false);

        return Response.ok().build();
    }

    @DELETE
    @Path("/template/package/{template_name}/{template_version}/workingcopy")
    @Timed
    @ApiOperation("Remove a workingcopy template package from cache")
    public Response clearTemplatePackageWorkingcopyCache(@Auth final User user,
                                                @PathParam("template_name") final String templateName,
                                                @PathParam("template_version") final String templateVersion) {
        LOGGER.info("Remove template package in workingcopy {}/{} from memory cache by {}.",
                templateName, templateVersion, user.getUsername());

        this.templatePackagesAggregate.removeFromCache(templateName, templateVersion, true);

        return Response.ok().build();
    }

    @DELETE
    @Path("/template/package/{template_name}/{template_version}/release")
    @Timed
    @ApiOperation("Remove a released template package from cache")
    public Response clearTemplatePackageReleaseCache(@Auth final User user,
                                                     @PathParam("template_name") final String templateName,
                                                     @PathParam("template_version") final String templateVersion) {
        LOGGER.info("Remove template package in release {}/{} from memory cache by {}.",
                templateName, templateVersion, user.getUsername());

        this.templatePackagesAggregate.removeFromCache(templateName, templateVersion, false);

        return Response.ok().build();
    }

    @DELETE
    @Path("/templates/packages")
    @Timed
    @ApiOperation("Remove all templates packages from cache")
    public Response clearTemplatesPackagesCache(@Auth final User user) {
        if (!user.isTechUser()) {
            throw new ForbiddenOperationException("Only tech user can clear all module templates packages cache.");
        }

        LOGGER.info("Remove templates packages from memory cache by {}.",
                user.getUsername());

        this.templatePackagesAggregate.removeAllCache();

        return Response.ok().build();
    }
}
