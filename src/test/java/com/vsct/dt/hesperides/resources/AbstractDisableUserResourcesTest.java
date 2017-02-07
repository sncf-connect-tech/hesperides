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
import javax.ws.rs.core.Response.Status;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import com.vsct.dt.hesperides.exception.wrapper.DefaultExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.DuplicateResourceExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.ForbiddenOperationExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.IllegalArgumentExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.IncoherentVersionExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.MissingResourceExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.OutOfDateVersionExceptionMapper;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.security.model.User;

/**
 * Created by emeric_martineau on 03/02/2017.
 */
public abstract class AbstractDisableUserResourcesTest {

    protected static ResourceTestRule createResource(final Binder binder, final Object resource) {
        return ResourceTestRule.builder()
                .addProvider(binder)
                .addResource(resource)
                .addProvider(new DefaultExceptionMapper())
                .addProvider(new DuplicateResourceExceptionMapper())
                .addProvider(new IncoherentVersionExceptionMapper())
                .addProvider(new OutOfDateVersionExceptionMapper())
                .addProvider(new MissingResourceExceptionMapper())
                .addProvider(new IllegalArgumentExceptionMapper())
                .addProvider(new ForbiddenOperationExceptionMapper())
                .build();

    }

    protected static ResourceTestRule createResourceWithContainer(final Binder binder, final Object resource, final TestContainerFactory container) {
        return ResourceTestRule.builder()
                .addProvider(binder)
                .setTestContainerFactory(container)
                .addResource(resource)
                .addProvider(new DefaultExceptionMapper())
                .addProvider(new DuplicateResourceExceptionMapper())
                .addProvider(new IncoherentVersionExceptionMapper())
                .addProvider(new OutOfDateVersionExceptionMapper())
                .addProvider(new MissingResourceExceptionMapper())
                .addProvider(new IllegalArgumentExceptionMapper())
                .addProvider(new ForbiddenOperationExceptionMapper())
                .build();

    }

    protected static ResourceTestRule createSimpleAuthResource(final Object object) {
        final Binder binder = AuthFactory.binder(new BasicAuthFactory<>(new SimpleAuthenticator(),
                "AUTHENTICATION_PROVIDER",
                User.class));

        return createResource(binder, object);
    }

    protected static ResourceTestRule createDisabledAuthResource(final Object object) {
        final Binder binder = AuthFactory.binder(new DisabledAuthProvider(User.UNTRACKED, User.class));

        return createResource(binder, object);
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
