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

package com.vsct.dt.hesperides.security.jersey;

import java.io.IOException;
import java.security.Principal;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * Created by emeric_martineau on 16/02/2017.
 */
public class NoCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoCredentialAuthFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        try {
            final Optional<P> principal = authenticator.authenticate(
                    new BasicCredentials("", "")
            );

            if (principal.isPresent()) {
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return principal.get();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return authorizer.authorize(principal.get(), role);
                    }

                    @Override
                    public boolean isSecure() {
                        return requestContext.getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }
                });
            }
        } catch (AuthenticationException e) {
            LOGGER.warn("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error decoding credentials", e);
        }

    }

    /**
     * Builder for {@link NoCredentialAuthFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
     *
     * @param <P> the principal
     */
    public static class Builder<P extends Principal> extends
            AuthFilterBuilder<BasicCredentials, P, NoCredentialAuthFilter<P>> {

        @Override
        protected NoCredentialAuthFilter<P> newInstance() {
            return new NoCredentialAuthFilter<>();
        }
    }
}
