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

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.vsct.dt.hesperides.security.model.User;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.auth.basic.BasicCredentials;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by william_montaz on 26/02/2015.
 */
public class BasicAuthProviderWithUserContextHolder extends BasicAuthProvider<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthProviderWithUserContextHolder.class);

    private final UserContext userContext;
    private final boolean     useDefaultUserIfAuthentFails;
    private final Authenticator<BasicCredentials, User> authenticator;
    private final String realm;

    /**
     * Creates a BasicauthProvider that can user a UserContextHolder to retrieve user infos somewhere else in the code
     * @param authenticator
     * @param realm
     * @param userContext
     */
    public BasicAuthProviderWithUserContextHolder(Authenticator<BasicCredentials, User> authenticator, String realm, UserContext userContext, boolean useDefaultUserWhenAuthentFails) {
        super(authenticator, realm);
        this.authenticator = authenticator;
        this.realm = realm;
        this.userContext = userContext;
        this.useDefaultUserIfAuthentFails = useDefaultUserWhenAuthentFails;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Auth a, Parameter c) {
        //Casting is guarantied by design of the basic auth provider class
        if(useDefaultUserIfAuthentFails){
            return new PutUserInContextWithDefaulting(authenticator, realm, a.required());
        } else {
            return new PutUserInContextWrapper((AbstractHttpContextInjectable<User>) super.getInjectable(ic, a, c), userContext);
        }
    }

    private static class PutUserInContextWrapper extends AbstractHttpContextInjectable<User> {

        private final AbstractHttpContextInjectable<User> injectable;
        private final UserContext                         userContext;

        public PutUserInContextWrapper(AbstractHttpContextInjectable<User> injectable, UserContext userContext) {
            this.injectable = injectable;
            this.userContext = userContext;
        }

        @Override
        public User getValue(HttpContext c) {
            User user = injectable.getValue(c);
            userContext.setCurrentUser(user);
            return user;
        }
    }

    /* Copy paste of inner class of BasicAuthProvider with little modifications
       The purpose is the migration of hesperides to https authenticated mode
       BasicAuthProvider does not allow to distinguish between authenticator failing and user avoiding to use basic credentials
        The modifications allow to distinguish between thoose cases
     */
    private static class PutUserInContextWithDefaulting extends AbstractHttpContextInjectable<User> {
        private static final String PREFIX = "Basic";
        private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";

        private final Authenticator<BasicCredentials, User> authenticator;
        private final String realm;
        private final boolean required;

        private PutUserInContextWithDefaulting(Authenticator<BasicCredentials, User> authenticator,
                                    String realm,
                                    boolean required) {
            this.authenticator = authenticator;
            this.realm = realm;
            this.required = required;
        }

        @Override
        public User getValue(HttpContext c) {
            final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
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
                                    return result.get();
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
            return User.UNTRACKED;
        }
    }
}
