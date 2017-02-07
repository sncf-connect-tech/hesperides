package com.vsct.dt.hesperides.resources;

import com.codahale.metrics.annotation.Timed;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.feedbacks.Acknowledge;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@Path("/feedback")
@Api("/feedback")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesFeedbackRessource {

    @Path("/sendmail")
    @POST
    @Timed
    @ApiOperation("Send an email feedback")
    public Acknowledge createRelease(@Auth final User user,
                                     @Valid final Template template) {

        System.out.println("Feedback OK");

        return new Acknowledge("OK");
    }
}
