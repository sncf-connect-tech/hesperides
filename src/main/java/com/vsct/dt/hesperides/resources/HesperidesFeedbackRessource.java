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
import com.vsct.dt.hesperides.security.model.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@Path("/feedback")
@Api("/feedback")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesFeedbackRessource {

    private final Feedbacks feedbacks;


    public HesperidesFeedbackRessource(final Feedbacks feedbacks) {
        this.feedbacks = feedbacks;
    }

    @Path("/hipchat")
    @POST
    @Timed
    @ApiOperation("Send an feedback to an hipchat room")
    public void feedbackHipchat(@Auth final User user,
                                @Valid final FeedbackJson feedbackJson) {

        this.feedbacks.sendFeedbackToHipchat(user, feedbackJson);

    }



}
