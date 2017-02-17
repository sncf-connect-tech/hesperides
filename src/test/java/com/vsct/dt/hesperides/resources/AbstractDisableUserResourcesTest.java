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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response.Status;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import com.vsct.dt.hesperides.exception.wrapper.DefaultExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.DuplicateResourceExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.ForbiddenExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.ForbiddenOperationExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.IllegalArgumentExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.IncoherentVersionExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.MissingResourceExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.OutOfDateVersionExceptionMapper;
import com.vsct.dt.hesperides.security.DisabledAuthenticator;
import com.vsct.dt.hesperides.security.jersey.NoCredentialAuthFilter;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.security.model.User;

/**
 * Created by emeric_martineau on 03/02/2017.
 */
public abstract class AbstractDisableUserResourcesTest {
    /**
     * Disable authentification -> return untracked.
     */
    private static final DisabledAuthenticator DISABLED_AUTHENTICATOR = new DisabledAuthenticator();

    /**
     * Simple authentification (just get username, we don't care about password) -> return username
     */
    private static final SimpleAuthenticator SIMPLE_AUTHENTICATOR = new SimpleAuthenticator();

    /**
     * Create a resource.
     *
     * @param userProvider
     * @param resource
     * @return
     */
    protected static ResourceTestRule createResource(final ContainerRequestFilter userProvider, final Object resource) {
        return ResourceTestRule.builder()
                .addResource(resource)
                .addProvider(RolesAllowedDynamicFeature.class)
                .addProvider(new AuthDynamicFeature(userProvider))
                .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
                .addProvider(new DefaultExceptionMapper())
                .addProvider(new DuplicateResourceExceptionMapper())
                .addProvider(new IncoherentVersionExceptionMapper())
                .addProvider(new OutOfDateVersionExceptionMapper())
                .addProvider(new MissingResourceExceptionMapper())
                .addProvider(new IllegalArgumentExceptionMapper())
                .addProvider(new ForbiddenOperationExceptionMapper())
                .addProvider(new ForbiddenExceptionMapper())
                .build();

    }

    protected static ResourceTestRule createResourceWithContainer(final ContainerRequestFilter userProvider, final Object resource, final TestContainerFactory
            container) {
        return ResourceTestRule.builder()
                .setTestContainerFactory(container)
                .addResource(resource)
                .addProvider(RolesAllowedDynamicFeature.class)
                .addProvider(new AuthDynamicFeature(userProvider))
                .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
                .addProvider(new DefaultExceptionMapper())
                .addProvider(new DuplicateResourceExceptionMapper())
                .addProvider(new IncoherentVersionExceptionMapper())
                .addProvider(new OutOfDateVersionExceptionMapper())
                .addProvider(new MissingResourceExceptionMapper())
                .addProvider(new IllegalArgumentExceptionMapper())
                .addProvider(new ForbiddenOperationExceptionMapper())
                .addProvider(new ForbiddenExceptionMapper())
                .build();

    }

    protected static ResourceTestRule createSimpleAuthResource(final Object object) {
        return createResource(
                new BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(SIMPLE_AUTHENTICATOR)
                        .setAuthorizer(SIMPLE_AUTHENTICATOR)
                        .setRealm("LOGIN AD POUR HESPERIDES")
                        .buildAuthFilter(),
                object);
    }

    protected static ResourceTestRule createDisabledAuthResource(final Object object) {
        return createResource(
                new NoCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(DISABLED_AUTHENTICATOR)
                    .setAuthorizer(DISABLED_AUTHENTICATOR)
                    .setRealm("LOGIN AD POUR HESPERIDES")
                    .buildAuthFilter(),
                object);
    }

    protected WebTarget withAuth(String url) {
        return getAuthResources().getJerseyTest().target(url);
    }

    protected WebTarget withoutAuth(String url) {
        return getDisabledAuthResources().getJerseyTest().target(url);
    }

    protected abstract ResourceTestRule getAuthResources();

    protected abstract ResourceTestRule getDisabledAuthResources();

    protected void check_bad_request_on_get_without_auth(final String path) {
        assertThat(
                withoutAuth(path)
                        .request()
                        .get()
                        .getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    protected void check_bad_request_on_delete_without_auth(final String path) {
        assertThat(
                withoutAuth(path)
                        .request()
                        .delete()
                        .getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    protected void check_bad_request_on_get_with_auth(final String path) {
        assertThat(
                withoutAuth(path)
                        .request()
                        .get()
                        .getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }
}
