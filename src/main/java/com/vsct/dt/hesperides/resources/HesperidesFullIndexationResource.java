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
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.command.*;
import com.vsct.dt.hesperides.indexation.mapper.ModuleMapper;
import com.vsct.dt.hesperides.indexation.mapper.PlatformMapper;
import com.vsct.dt.hesperides.indexation.mapper.TemplateMapper;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 04/02/2015.
 */
@Path("/indexation")
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
     * A nice way to do this would be to block application until indexation is finished
     * @throws FileNotFoundException
     */
    @Path("/perform_reindex")
    @POST
    @Timed
    @ApiOperation("Reindex all applications, modules, templates...")
    public void resetIndex() throws IOException {

        LOGGER.info("RELOADING INDEX");
        elasticSearchIndexationExecutor.reset();

        elasticSearchIndexationExecutor.index(new IndexNewTemplateCommandBulk(templatePackages.getAll().stream().map(template -> TemplateMapper.asTemplateIndexation(template)).collect(Collectors.toList())));

        elasticSearchIndexationExecutor.index(new IndexNewModuleCommandBulk(modules.getAllModules().stream().map(module -> ModuleMapper.toModuleIndexation(module)).collect(Collectors.toList())));

        elasticSearchIndexationExecutor.index(new IndexNewTemplateCommandBulk(modules.getAll().stream().map(template -> TemplateMapper.asTemplateIndexation(template)).collect(Collectors.toList())));

        elasticSearchIndexationExecutor.index(new IndexNewPlatformCommandBulk(applications.getAll().stream().map(app -> PlatformMapper.asPlatformIndexation(app)).collect(Collectors.toList())));

    }

}
