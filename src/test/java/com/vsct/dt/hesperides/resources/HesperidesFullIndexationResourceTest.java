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

package com.vsct.dt.hesperides.resources;

import javax.ws.rs.core.Response.Status;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.UniformInterfaceException;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;

import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.exception.wrapper.ForbiddenOperationExceptionMapper;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;

/**
 * Created by william_montaz on 01/09/14.
 */
/* AUTHENTICATION -> John_Doe:secret => Basic Sm9obl9Eb2U6c2VjcmV0 */
public class HesperidesFullIndexationResourceTest {

    private static final ModulesAggregate MODULES_AGGREGATE = mock(ModulesAggregate.class);
    private static final TemplatePackagesAggregate TEMPLATE_PACKAGES_AGGREGATE = mock(TemplatePackagesAggregate.class);
    private static final ApplicationsAggregate APPLICATIONS_AGGREGATE = mock(ApplicationsAggregate.class);
    private static final ElasticSearchIndexationExecutor ELASTIC_SEARCH_INDEXATION_EXECUTOR = mock(ElasticSearchIndexationExecutor.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private static class TechUserAuthenticator implements Authenticator<BasicCredentials, User> {
        private static final User USER = new User("tech", false, true);

        @Override
        public com.google.common.base.Optional<User> authenticate(final BasicCredentials basicCredentials) throws AuthenticationException {
            return com.google.common.base.Optional.of(USER);
        }
    }

    private static class NoTechUserAuthenticator implements Authenticator<BasicCredentials, User> {
        private static final User USER = new User("tech", false, false);

        @Override
        public com.google.common.base.Optional<User> authenticate(final BasicCredentials basicCredentials) throws AuthenticationException {
            return com.google.common.base.Optional.of(USER);
        }
    }

    @ClassRule
    public static ResourceTestRule techAuthResources = ResourceTestRule.builder()
            .addProvider(new BasicAuthProvider<>(
                    new TechUserAuthenticator(),
                    "AUTHENTICATION_PROVIDER"))
            .addResource(new HesperidesFullIndexationResource(ELASTIC_SEARCH_INDEXATION_EXECUTOR, APPLICATIONS_AGGREGATE, MODULES_AGGREGATE,
                    TEMPLATE_PACKAGES_AGGREGATE))
            .addProvider(new ForbiddenOperationExceptionMapper())
            .build();

    @ClassRule
    public static ResourceTestRule noTechAuthResources = ResourceTestRule.builder()
            .addProvider(new BasicAuthProvider<>(
                    new NoTechUserAuthenticator(),
                    "AUTHENTICATION_PROVIDER"))
            .addResource(new HesperidesFullIndexationResource(ELASTIC_SEARCH_INDEXATION_EXECUTOR, APPLICATIONS_AGGREGATE, MODULES_AGGREGATE,
                    TEMPLATE_PACKAGES_AGGREGATE))
            .addProvider(new ForbiddenOperationExceptionMapper())
            .build();


    public com.sun.jersey.api.client.WebResource.Builder withTechAuth(String url) {
        return techAuthResources.client().resource(url).header("Authorization", "Basic Sm9obl9Eb2U6c2VjcmV0");
    }

    public com.sun.jersey.api.client.WebResource.Builder withNoTechAuth(String url) {
        return noTechAuthResources.client().resource(url).header("Authorization", "Basic Sm9obl9Eb2U6c2VjcmV0");
    }


    @Before
    public void setup() {
        reset(MODULES_AGGREGATE);
        reset(APPLICATIONS_AGGREGATE);
        reset(TEMPLATE_PACKAGES_AGGREGATE);
    }

    @Test
    public void should_return_403_forbiden_when_clear_applications_caches() {
        try {
            withNoTechAuth("/indexation/perform_reindex")
                    .post();
            fail("Ne renvoie pas le status 403");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
        }
    }

    @Test
    public void should_return_works_when_clear_applications_caches() {
        try {
            withTechAuth("/indexation/perform_reindex")
                    .post();
        } catch (UniformInterfaceException e) {
            fail("Le service devrait fonctionner");
        }
    }
}
