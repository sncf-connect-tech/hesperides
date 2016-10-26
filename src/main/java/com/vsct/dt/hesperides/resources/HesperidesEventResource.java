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
import com.vsct.dt.hesperides.events.EventData;
import com.vsct.dt.hesperides.events.Events;
import com.vsct.dt.hesperides.security.model.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by tidiane_sidibe on 01/03/2016.
 *
 * This exposes services for operations on events.
 */

@Path("/events")
@Api("/events")
public class HesperidesEventResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesFilesResource.class);
    private final Events events;
    public HesperidesEventResource (final Events events){
        this.events = events;
    }

    @Path("/{stream_name}")
    @GET
    @Timed
    @ApiOperation(
            value = "Gets the events list for a stream name of platform or module.",
            notes = "Example platform-VSA-INT3 for plaforme, module-demoKatana-war-1.0.0.0-wc for a module.\n" +
                    "The default page number is 1 and the default pagination size is 25"
    )
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventData> getEventsList(@Auth User user, @PathParam("stream_name") final String streamName, @QueryParam("page") final int page, @QueryParam("size") final int size){

        return this.events.getEventsList(streamName, page, size);
    }
}