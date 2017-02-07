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

import com.google.common.base.Optional;


import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import com.vsct.dt.hesperides.security.ThreadLocalUserContext;
import com.vsct.dt.hesperides.security.model.User;

/**
 * Created by emeric_martineau on 01/02/2017.
 */
public class HesperidesAuthenticator implements Authenticator<BasicCredentials, User> {

    private final java.util.Optional<Authenticator<BasicCredentials, User>> authenticator;
    private final ThreadLocalUserContext userContext;

    public HesperidesAuthenticator(final java.util.Optional<Authenticator<BasicCredentials, User>> authenticator,
            final ThreadLocalUserContext userContext) {
        this.authenticator = authenticator;
        this.userContext = userContext;
    }

    @Override
    public Optional<User> authenticate(final BasicCredentials credentials) throws AuthenticationException {
        Optional<User> user;

        if (this.authenticator.isPresent()) {
            user = this.authenticator.get().authenticate(credentials);
        } else {
            user = Optional.of(User.UNTRACKED);
        }

        this.userContext.setCurrentUser(user.get());

        return user;
    }
}
