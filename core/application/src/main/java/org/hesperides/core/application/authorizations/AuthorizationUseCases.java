/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
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
package org.hesperides.core.application.authorizations;

import org.hesperides.core.domain.security.entities.authorities.ApplicationRole;
import org.hesperides.core.domain.security.queries.AuthorizationQueries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthorizationUseCases {

    private final AuthorizationQueries authorizationQueries;

    public AuthorizationUseCases(AuthorizationQueries authorizationQueries) {
        this.authorizationQueries = authorizationQueries;
    }

    public Map<String, List<String>> getApplicationAuthorities(String applicationName) {
        Map<String, List<String>> applicationAuthorities = new HashMap<>();

        String applicationProdRole = applicationName + ApplicationRole.PROD_USER_SUFFIX;
        applicationAuthorities.put(applicationProdRole, authorizationQueries.getApplicationAuthorities(applicationName));

        return applicationAuthorities;
    }
}
