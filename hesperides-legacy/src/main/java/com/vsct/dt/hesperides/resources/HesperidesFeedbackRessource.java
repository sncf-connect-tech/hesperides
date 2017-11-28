/*
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.vsct.dt.hesperides.resources;

import com.codahale.metrics.annotation.Timed;
import com.vsct.dt.hesperides.feedback.Feedbacks;
import com.vsct.dt.hesperides.feedback.jsonObject.FeedbackJson;
import com.vsct.dt.hesperides.security.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@Path("/feedback")
@Api("/feedback")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesFeedbackRessource {

    private final Feedbacks feedbacks;

    /**
     * Response when not avaible.
     */
    private final Response notAvailable;

    public HesperidesFeedbackRessource(final Feedbacks feedbacks) {
        this.feedbacks = feedbacks;

        this.notAvailable = Response.status(Status.SERVICE_UNAVAILABLE).entity("No feedback configuration avaible").build();
    }

    @Path("/hipchat")
    @POST
    @Timed
    @ApiOperation("Send an feedback to an hipchat room.")
    public Response feedbackHipchat(@Auth final User user, @Valid final FeedbackJson feedbackJson) {
        Response r;

        if (this.feedbacks == null) {
            r = this.notAvailable;
        } else {
            this.feedbacks.sendFeedbackToHipchat(user, feedbackJson);
            r = Response.noContent().build();
        }

        return r;
    }
}
