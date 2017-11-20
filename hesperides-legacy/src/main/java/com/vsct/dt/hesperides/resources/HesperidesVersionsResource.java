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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.jackson.JsonSnakeCase;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by william_montaz on 12/01/2015.
 */
@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api("/versions")
public class HesperidesVersionsResource {

    private final Versions versions;

    public HesperidesVersionsResource(String backendVersion, String APIVersion){
        this.versions = new Versions(backendVersion, APIVersion);
    }

    @Path("/")
    @GET
    @Timed
    @ApiOperation(
            value = "Get backend and API versions",
            notes = "These versions are provided just for information"
    )
    public Versions getVersions() {
        return this.versions;
    }

    @JsonSnakeCase
    public static class Versions {

        private final String backendVersion;
        private final String apiVersion;

        @JsonCreator
        public Versions(@JsonProperty("backend_version") String backendVersion, @JsonProperty("api_version") String apiVersion) {
            this.backendVersion = backendVersion;
            this.apiVersion = apiVersion;
        }

        public String getBackendVersion() {
            return backendVersion;
        }

        public String getApiVersion() {
            return apiVersion;
        }
    }
}
