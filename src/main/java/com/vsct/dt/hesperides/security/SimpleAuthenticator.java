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

import java.util.Optional;
import com.vsct.dt.hesperides.security.model.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * Created by william_montaz on 12/11/2014.
 */
public final class SimpleAuthenticator implements Authenticator<BasicCredentials, User>, Authorizer<User> {
    @Override
    public Optional<User> authenticate(final BasicCredentials basicCredentials) throws AuthenticationException {
        return Optional.of(new User(basicCredentials.getUsername(), true, true));
    }

    @Override
    public boolean authorize(final User user, final String role) {
        return true;
    }
}
