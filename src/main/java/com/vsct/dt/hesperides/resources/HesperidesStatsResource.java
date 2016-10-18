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
import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.jackson.JsonSnakeCase;
import scala.Int;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by tidiane_sidibe on 16/09/2016.
 *
 * Inspired by Sahar Chaillou
 *
 * This is used to get some statistics information such as number of apps, platforms, ...
 *
 */

@Path("/stats")
@Api("/stats")
public class HesperidesStatsResource {

    private final Applications applicationsAggregate;
    private final Modules modulesAggregate;

    public HesperidesStatsResource (final Applications applicationsAggregate, final Modules modulesAggregate){
        this.applicationsAggregate = applicationsAggregate;
        this.modulesAggregate = modulesAggregate;
    }

    @GET
    @Timed
    @ApiOperation("Get some statics data about Hesperides")
    @Produces(MediaType.APPLICATION_JSON)
    public Stats getStats (){
        return new Stats(applicationsAggregate.getAllApplicationsCount(), applicationsAggregate.getAllPlatformsCount(), applicationsAggregate.getAllModulesCount(), modulesAggregate.getAllTechnosCount());
    }

    /**
     *  Internal class
     *  This is an internal class for holding statistics
     */
    @JsonSnakeCase
    public static class Stats {

        private final Integer numberOfApplications;
        private final Integer numberOfPlatforms;
        private final Integer numberOfModules;
        private final Integer numberOfTechnos;

        public Stats(final Integer numberOfApplications, final Integer numberOfPlatforms, final Integer numberOfModules, final Integer numberOfTechnos) {
            this.numberOfPlatforms = numberOfPlatforms;
            this.numberOfApplications = numberOfApplications;
            this.numberOfModules = numberOfModules;
            this.numberOfTechnos = numberOfTechnos;
        }

        public Integer getNumberOfPlatforms() {
            return numberOfPlatforms;
        }

        public Integer getNumberOfApplications() {
            return numberOfApplications;
        }

        public Integer getNumberOfModules() {
            return numberOfModules;
        }

        public Integer getNumberOfTechnos() {
            return numberOfTechnos;
        }
    }
}
