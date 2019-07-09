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

import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.security.commands.AuthorizationCommands;
import org.hesperides.core.domain.security.entities.ApplicationAuthorities;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.exceptions.CreateAuthoritiesForbiddenException;
import org.hesperides.core.domain.security.exceptions.UpdateAuthoritiesForbiddenException;
import org.hesperides.core.domain.security.queries.AuthorizationQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationAuthoritiesView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthorizationUseCases {

    private final AuthorizationQueries authorizationQueries;
    private final AuthorizationCommands authorizationCommands;
    private final PlatformQueries platformQueries;

    public AuthorizationUseCases(AuthorizationQueries authorizationQueries, AuthorizationCommands authorizationCommands, PlatformQueries platformQueries) {
        this.authorizationQueries = authorizationQueries;
        this.authorizationCommands = authorizationCommands;
        this.platformQueries = platformQueries;
    }

    public Optional<ApplicationAuthoritiesView> getApplicationAuthorities(String applicationName) {
        return authorizationQueries.getApplicationAuthorities(applicationName);
    }

    public void createOrUpdateApplicationAuthorities(String applicationName, Map<String, List<String>> authorities, User user) {
        if (platformQueries.applicationExists(applicationName)) {
            throw new ApplicationNotFoundException(applicationName);
        }

        final ApplicationAuthorities providedApplicationAuthorities = new ApplicationAuthorities(applicationName, authorities);

        final Optional<ApplicationAuthoritiesView> existingApplicationAuthorities = authorizationQueries.getApplicationAuthorities(applicationName);
        if (existingApplicationAuthorities.isPresent()) {
            // L'utilisateur doit avoir les droits de prod ou appartenir à l'un
            // des groupes AD de l'application pour modifier ses "authorities"
            if (!user.isGlobalProd() && user.hasAtLeastOneAuthority(authorities)) {
                throw new UpdateAuthoritiesForbiddenException(applicationName);
            }
            authorizationCommands.updateApplicationAuthorities(existingApplicationAuthorities.get().getId(), providedApplicationAuthorities, user);
        } else {
            // L'utilisateur doit avoir les droits de prod pour
            // créer les authorities de cette application
            if (!user.isGlobalProd()) {
                throw new CreateAuthoritiesForbiddenException(applicationName);
            }
            authorizationCommands.createApplicationAuthorities(providedApplicationAuthorities, user);
        }
    }
}
