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


import javax.servlet.http.HttpServletRequest;

/**
 * Disable auth for Hesperides.
 *
 * @param <T> the principal type.
 */
public final class DisabledAuthProvider<T> extends AuthFactory<BasicCredentials, T> {

    private T user;
    private final Class<T> generatedClass;

    public DisabledAuthProvider(final T user, final Class<T> generatedClass) {
        super(null);
        this.user = user;
        this.generatedClass = generatedClass;
    }

    @Override
    public AuthFactory<BasicCredentials, T> clone(boolean required) {
        return new DisabledAuthProvider(this.user, this.generatedClass);
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        // Nothing
    }

    @Override
    public T provide() {
        return user;
    }

    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}
