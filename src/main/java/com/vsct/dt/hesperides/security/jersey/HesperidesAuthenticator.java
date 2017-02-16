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

import java.util.Optional;


import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import com.vsct.dt.hesperides.security.ThreadLocalUserContext;
import com.vsct.dt.hesperides.security.model.User;

/**
 * Authenticator for Hesperides.
 *
 * Created by emeric_martineau on 01/02/2017.
 */
public class HesperidesAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {
    /**
     * Thread local context to set user.
     */
    private final ThreadLocalUserContext userContext;

    /**
     * All authentication use cache.
     * If null, no authenticator set.
     */
    private CachingAuthenticator<BasicCredentials, User> cachingAuthenticator = null;


    protected HesperidesAuthenticator() {
        // Only for test
        this.userContext = null;
    }

    /**
     *
     * @param authenticator authenticator
     * @param userContext local thread user
     * @param metrics metric system
     * @param authenticationCachePolicy cache policy
     */
    public HesperidesAuthenticator(final Optional<Authenticator<BasicCredentials, User>> authenticator,
            final ThreadLocalUserContext userContext, final MetricRegistry metrics, final CacheBuilderSpec authenticationCachePolicy) {
        this.userContext = userContext;

        if (authenticator.isPresent()) {
            this.cachingAuthenticator = new CachingAuthenticator<>(metrics, authenticator.get(), authenticationCachePolicy);
        }
    }

    @Override
    public Optional<User> authenticate(final BasicCredentials credentials) throws AuthenticationException {
        Optional<User> user;

        if (cachingAuthenticator == null) {
            user = Optional.of(User.UNTRACKED);
        } else {
            user = this.cachingAuthenticator.authenticate(credentials);
        }

        if (user.isPresent()) {
            this.userContext.setCurrentUser(user.get());
        }

        return user;
    }

    @Override
    public boolean authorize(final User user, final String role) {
        return User.TECH.equals(role) && user.isTechUser();
    }
}
