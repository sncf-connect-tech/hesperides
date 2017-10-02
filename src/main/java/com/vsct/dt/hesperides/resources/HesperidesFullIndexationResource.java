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
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.exception.runtime.ForbiddenOperationException;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.command.*;
import com.vsct.dt.hesperides.indexation.mapper.ModuleMapper;
import com.vsct.dt.hesperides.indexation.mapper.PlatformMapper;
import com.vsct.dt.hesperides.indexation.mapper.TemplateMapper;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 04/02/2015.
 */
@Path("/indexation")
@Api("/indexation")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesFullIndexationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesFullIndexationResource.class);
    private final ElasticSearchIndexationExecutor elasticSearchIndexationExecutor;
    private final TemplatePackagesAggregate       templatePackages;
    private final ModulesAggregate                modules;
    private final ApplicationsAggregate           applications;

    public HesperidesFullIndexationResource(ElasticSearchIndexationExecutor elasticSearchIndexationExecutor, ApplicationsAggregate applications, ModulesAggregate modules, TemplatePackagesAggregate templatePackages) {
        this.elasticSearchIndexationExecutor = elasticSearchIndexationExecutor;
        this.templatePackages = templatePackages;
        this.modules = modules;
        this.applications = applications;
    }

    /**
     * Reindex the full application
     * We will have problems if someone calls resetIndex many times
     * We assume no one wants to do that...
     * A nice way to do this would be to block application until indexation is finished.
     *
     * @throws FileNotFoundException
     */
    @Path("/all")
    @POST
    @Timed
    @ApiOperation("Reindex all applications, modules, templates...")
    @RolesAllowed(User.TECH)
    public void resetIndex(@Auth final User user) throws IOException {
        reset(user);
    }

    /**
     * Regenerate ElasticSearch Mapping.
     */
    @Path("/mapping")
    @POST
    @Timed
    @ApiOperation("Regenerate mapping in ElasticSearch")
    @RolesAllowed(User.TECH)
    public void mapping(@Auth final User user) throws IOException {
        elasticSearchIndexationExecutor.remapping();
    }

    /**
     * Reindex template package.
     */
    @Path("/templates/packages")
    @POST
    @Timed
    @ApiOperation("Reindex template package")
    @RolesAllowed(User.TECH)
    public void templatesPackages(@Auth final User user) throws IOException {
        elasticSearchIndexationExecutor.index(
                new IndexNewTemplateCommandBulk(
                        templatePackages
                                .getAllTemplates()
                                .stream()
                                .map(template -> TemplateMapper.asTemplateIndexation(template))
                                .collect(Collectors.toList())));
    }

    /**
     * Reindex module.
     */
    @Path("/modules")
    @POST
    @Timed
    @ApiOperation("Reindex modules and templates")
    @RolesAllowed(User.TECH)
    public void modules(@Auth final User user) throws IOException {
        elasticSearchIndexationExecutor.index(
                new IndexNewModuleCommandBulk(
                        modules
                                .getAllModules()
                                .stream()
                                .map(module -> ModuleMapper.toModuleIndexation(module))
                                .collect(Collectors.toList())));

        modulesTemplates(user);
    }

    /**
     * Reindex templates's modules.
     */
    @Path("/modules/templates")
    @POST
    @Timed
    @ApiOperation("Reindex only template of modules")
    @RolesAllowed(User.TECH)
    public void modulesTemplates(@Auth final User user) throws IOException {
        elasticSearchIndexationExecutor.index(
                new IndexNewTemplateCommandBulk(
                        modules
                                .getAll()
                                .stream()
                                .map(template -> TemplateMapper.asTemplateIndexation(template))
                                .collect(Collectors.toList())));
    }

    /**
     * Reindex applications.
     */
    @Path("/applications")
    @POST
    @Timed
    @ApiOperation("Reindex applications and platforms")
    @RolesAllowed(User.TECH)
    public void applications(@Auth final User user) throws IOException {
        elasticSearchIndexationExecutor.index(
                new IndexNewPlatformCommandBulk(
                        applications
                                .getAllPlatforms()
                                .stream()
                                .map(app -> PlatformMapper.asPlatformIndexation(app))
                                .collect(Collectors.toList())));
    }

    /**
     * Internal call for reindexation
     *
     * @throws IOException
     */
    public void resetIndex() throws IOException {
        reset(null);
    }

    /**
     * Internal call for reindexation
     * @param user
     * @throws IOException
     */
    private void reset(final User user) throws IOException {
        if (user == null) {
            LOGGER.info("RELOADING INDEX START at startup");
        } else {
            LOGGER.info("RELOADING INDEX START by {}", user.getName());
        }

        elasticSearchIndexationExecutor.reset();

        templatesPackages(user);

        modules(user);

        applications(user);

        LOGGER.info("RELOADING INDEX END");
    }
}
