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
import com.vsct.dt.hesperides.security.model.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by tidiane_sidibe on 17/05/2016.
 *
 * This is used to get information about the authenticated user.
 * It's called by the UI at the start time.
 */

@Path("/users")
@Api("/users")
public class HesperidesUserResource extends  BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesFilesResource.class);

    @Path("/auth")
    @GET
    @Timed
    @ApiOperation("Authenticates users. It returns useful information about the authenticated user.")
    @Produces(MediaType.APPLICATION_JSON)
    public User authenticate (@Auth User user){
        LOGGER.debug(" Authenticated User : '{}' is producation user : {}.", user.getUsername(), user.isProdUser() ? "YES" : "NO");
        return user;
    }
}
