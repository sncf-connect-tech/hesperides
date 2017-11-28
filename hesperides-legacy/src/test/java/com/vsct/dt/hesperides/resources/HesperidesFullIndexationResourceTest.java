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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.exception.wrapper.ForbiddenOperationExceptionMapper;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * Created by william_montaz on 01/09/14.
 */
/* AUTHENTICATION -> John_Doe:secret => Basic Sm9obl9Eb2U6c2VjcmV0 */
@Category(UnitTests.class)
public class HesperidesFullIndexationResourceTest {

    private static final String CREDENTIALS = "Sm9obl9Eb2U6c2VjcmV0";

    private static final ModulesAggregate MODULES_AGGREGATE = mock(ModulesAggregate.class);
    private static final TemplatePackagesAggregate TEMPLATE_PACKAGES_AGGREGATE = mock(TemplatePackagesAggregate.class);
    private static final ApplicationsAggregate APPLICATIONS_AGGREGATE = mock(ApplicationsAggregate.class);
    private static final ElasticSearchIndexationExecutor ELASTIC_SEARCH_INDEXATION_EXECUTOR = mock(ElasticSearchIndexationExecutor.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private static final BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER =
            new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new SimpleAuthenticator())
                    .setPrefix("Basic")
                    .setRealm("AUTHENTICATION_PROVIDER")
                    .buildAuthFilter();
    @ClassRule
    public static ResourceTestRule simpleAuthResources = ResourceTestRule.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .addResource(new HesperidesFullIndexationResource(ELASTIC_SEARCH_INDEXATION_EXECUTOR, APPLICATIONS_AGGREGATE, MODULES_AGGREGATE,
                    TEMPLATE_PACKAGES_AGGREGATE))
            .addProvider(new ForbiddenOperationExceptionMapper())
            .build();


    public WebTarget rawClient(String url) {
        return simpleAuthResources.client().target(url);
    }

    public Invocation.Builder withAuth(String url) {
        return rawClient(url).request();
    }

    public Invocation.Builder withoutAuth(String url) {
        return withAuth(url).header("Authorization", "Basic " + CREDENTIALS);
    }


    @Before
    public void setup() {
        reset(MODULES_AGGREGATE);
        reset(APPLICATIONS_AGGREGATE);
        reset(TEMPLATE_PACKAGES_AGGREGATE);
    }

    @Test
    public void should_return_401_unauthorized_when_clear_applications_caches() {
            Response response = withAuth("/indexation/perform_reindex").post(null);
            assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_return_works_when_clear_applications_caches() {
        try {
            withoutAuth("/indexation/perform_reindex")
                    .post(Entity.json(null));
        } catch (ResponseProcessingException e) {
            fail("Le service devrait fonctionner");
        }
    }
}
