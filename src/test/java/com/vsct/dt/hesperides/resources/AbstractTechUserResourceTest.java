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

import java.util.Map;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import com.google.common.collect.ImmutableMap;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.testing.junit.ResourceTestRule;

import com.vsct.dt.hesperides.security.model.User;

/**
 * Created by emeric_martineau on 03/02/2017.
 *
 * tech_user -> Basic dGVjaF91c2VyOnBhc3N3b3Jk
 * no_tech_user -> Basic bm9fdGVjaF91c2VyOnBhc3N3b3Jk
 */
public abstract class AbstractTechUserResourceTest extends AbstractDisableUserResourcesTest {
    /**
     * Class to manage tech user or no tech user.
     */
    private static class TechUserAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {
        private static final Map<String, User> USERS = ImmutableMap.of(
                "tech_user", new User("tech_user", false, true),
                "no_tech_user", new User("no_tech_user", false, false));

        @Override
        public com.google.common.base.Optional<User> authenticate(final BasicCredentials basicCredentials) throws AuthenticationException {
            return com.google.common.base.Optional.of(USERS.get(basicCredentials.getUsername()));
        }

        @Override
        public boolean authorize(final User user, final String role) {
            return true;
        }
    }

    /**
     * Default tech/no tech autenticator.
     */
    protected static TechUserAuthenticator TECH_NO_TECH_AUTHENTICATOR = new TechUserAuthenticator();

    /**
     * Http server.
     */
    protected static final TestContainerFactory DEFAULT_CONTAINER = new GrizzlyWebTestContainerFactory();

    /**
     * Create request for no tech user.
     *
     * @param url url to test
     *
     * @return return builder of request.
     */
    protected Builder withTechAuth(final String url) {
        return getAuthResources().getJerseyTest().target(url).request().header(HttpHeaders.AUTHORIZATION, "Basic dGVjaF91c2VyOnBhc3N3b3Jk");
    }

    /**
     * Create request for tech user.
     *
     * @param url url to test
     *
     * @return return builder of request.
     */
    protected Builder withNoTechAuth(final String url) {
        return getAuthResources().getJerseyTest().target(url).request().header(HttpHeaders.AUTHORIZATION, "Basic bm9fdGVjaF91c2VyOnBhc3N3b3Jk");
    }

    /**
     * Create context to test resource with authentication.
     *
     * @param resource resource Hesperides***Resource
     *
     * @return test rule resource
     */
    protected static ResourceTestRule createAuthenticationResource(final Object resource) {
        return createResourceWithContainer(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(TECH_NO_TECH_AUTHENTICATOR)
                        .setAuthorizer(TECH_NO_TECH_AUTHENTICATOR)
                        .setRealm("LOGIN AD POUR HESPERIDES")
                        .buildAuthFilter(),
                resource, DEFAULT_CONTAINER);
    }

    @Override
    protected ResourceTestRule getDisabledAuthResources() {
        return null;
    }
}
