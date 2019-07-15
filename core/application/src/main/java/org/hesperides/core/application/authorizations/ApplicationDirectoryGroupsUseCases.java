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
import org.hesperides.core.domain.security.commands.ApplicationDirectoryGroupsCommands;
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.exceptions.CreateDirectoryGroupsForbiddenException;
import org.hesperides.core.domain.security.exceptions.UpdateDirectoryGroupsForbiddenException;
import org.hesperides.core.domain.security.queries.ApplicationDirectoryGroupsQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ApplicationDirectoryGroupsUseCases {

    private final ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries;
    private final ApplicationDirectoryGroupsCommands applicationDirectoryGroupsCommands;
    private final PlatformQueries platformQueries;

    public ApplicationDirectoryGroupsUseCases(ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries,
                                              ApplicationDirectoryGroupsCommands applicationDirectoryGroupsCommands,
                                              PlatformQueries platformQueries) {
        this.applicationDirectoryGroupsQueries = applicationDirectoryGroupsQueries;
        this.applicationDirectoryGroupsCommands = applicationDirectoryGroupsCommands;
        this.platformQueries = platformQueries;
    }

    public Optional<ApplicationDirectoryGroupsView> getApplicationDirectoryGroups(String applicationName) {
        return applicationDirectoryGroupsQueries.getApplicationDirectoryGroups(applicationName);
    }

    public void setApplicationDirectoryGroups(String applicationName, Map<String, List<String>> directoryGroups, User user) {
        if (!platformQueries.applicationExists(applicationName)) {
            throw new ApplicationNotFoundException(applicationName);
        }

        final ApplicationDirectoryGroups providedApplicationDirectoryGroups = new ApplicationDirectoryGroups(applicationName, directoryGroups);

        final Optional<ApplicationDirectoryGroupsView> existingApplicationDirectoryGroups = applicationDirectoryGroupsQueries.getApplicationDirectoryGroups(applicationName);
        if (existingApplicationDirectoryGroups.isPresent()) {
            // L'utilisateur doit avoir les droits de prod ou appartenir à l'un
            // des groupes AD de l'application pour modifier ses "directory groups"
            if (!user.isGlobalProd() && user.hasAtLeastOneDirectoryGroup(directoryGroups)) {
                throw new UpdateDirectoryGroupsForbiddenException(applicationName);
            }
            applicationDirectoryGroupsCommands.updateApplicationDirectoryGroups(existingApplicationDirectoryGroups.get().getId(), providedApplicationDirectoryGroups, user);
        } else {
            // L'utilisateur doit avoir les droits de prod pour
            // créer les directory groups de cette application
            if (!user.isGlobalProd()) {
                throw new CreateDirectoryGroupsForbiddenException(applicationName);
            }
            applicationDirectoryGroupsCommands.createApplicationDirectoryGroups(providedApplicationDirectoryGroups, user);
        }
    }
}
