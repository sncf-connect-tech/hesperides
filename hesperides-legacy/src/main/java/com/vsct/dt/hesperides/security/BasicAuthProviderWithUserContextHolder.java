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

package com.vsct.dt.hesperides.security;

import com.vsct.dt.hesperides.api.authentication.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by william_montaz on 26/02/2015.
 */
@Provider
public class BasicAuthProviderWithUserContextHolder implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthProviderWithUserContextHolder.class);

    private UserContext userContext;

    private final boolean useDefaultUserIfAuthentFails;
    private final Authenticator<BasicCredentials, User> authenticator;
    private final String realm;
    private static final String PREFIX = "Basic";
    private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";
    private final boolean required = true;

    /**
     * Creates a BasicauthProvider that can user a UserContextHolder to retrieve user infos somewhere else in the code
     *
     * @param authenticator
     * @param realm
     * @param userContext
     */
    public BasicAuthProviderWithUserContextHolder(Authenticator<BasicCredentials, User> authenticator, String realm, UserContext userContext, boolean useDefaultUserWhenAuthentFails) {
//        super(authenticator, realm);
        this.authenticator = authenticator;
        this.realm = realm;
        this.userContext = userContext;
        this.useDefaultUserIfAuthentFails = useDefaultUserWhenAuthentFails;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        try {
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (PREFIX.equalsIgnoreCase(method)) {
                        final String decoded = B64Code.decode(header.substring(space + 1),
                                StringUtil.__ISO_8859_1);
                        final int i = decoded.indexOf(':');
                        if (i > 0) {
                            final String username = decoded.substring(0, i);
                            final String password = decoded.substring(i + 1);
                            final BasicCredentials credentials = new BasicCredentials(username,
                                    password);
                            final Optional<User> result = authenticator.authenticate(credentials);
                            if (result.isPresent()) {
                                userContext.setCurrentUser(result.get());
                            } else {
                                if (required) {
                                    final String challenge = String.format(CHALLENGE_FORMAT, realm);
                                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                                            .header(HttpHeaders.WWW_AUTHENTICATE,
                                                    challenge)
                                            .entity("Valid credentials are required to access this resource.")
                                            .type(MediaType.TEXT_PLAIN_TYPE)
                                            .build());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Error decoding credentials", e);
        } catch (AuthenticationException e) {
            LOGGER.warn("Error authenticating credentials", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        //No header voluntarly, defaulting
//            return User.UNTRACKED;
    }
}
