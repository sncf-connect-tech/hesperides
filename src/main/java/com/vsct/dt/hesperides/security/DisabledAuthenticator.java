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

import io.dropwizard.auth.*;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.Optional;

import com.vsct.dt.hesperides.security.model.User;

/**
 * Disable auth for Hesperides.
 */
public final class DisabledAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {

    private final static Optional<User> USER = Optional.of(User.UNTRACKED);

    @Override
    public Optional<User> authenticate(final BasicCredentials basicCredentials) throws AuthenticationException {
        return USER;
    }

    @Override
    public boolean authorize(final User user, final String role) {
        return true;
    }
}
