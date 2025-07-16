/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.domain.security.entities.springauthorities;

import org.springframework.security.core.GrantedAuthority;

public class ApplicationProdRole implements GrantedAuthority {

    public static final String PROD_USER_SUFFIX = "_PROD_USER";

    private final String applicationName;

    public ApplicationProdRole(String applicationName) {
        this.applicationName = applicationName + PROD_USER_SUFFIX;
    }

    @Override
    public String getAuthority() {
        return applicationName;
    }
}
