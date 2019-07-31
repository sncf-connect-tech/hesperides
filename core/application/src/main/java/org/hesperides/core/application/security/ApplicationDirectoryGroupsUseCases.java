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
package org.hesperides.core.application.security;

import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.security.commands.ApplicationDirectoryGroupsCommands;
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.entities.springauthorities.ApplicationProdRole;
import org.hesperides.core.domain.security.exceptions.CreateDirectoryGroupsForbiddenException;
import org.hesperides.core.domain.security.exceptions.InvalidDirectoryGroupsException;
import org.hesperides.core.domain.security.exceptions.UpdateDirectoryGroupsForbiddenException;
import org.hesperides.core.domain.security.queries.ApplicationDirectoryGroupsQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.domain.security.queries.views.DirectoryGroupsView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        directoryGroups = removeDuplicatedCNs(directoryGroups);
        final ApplicationDirectoryGroups providedApplicationDirectoryGroups = validateAndBuildDirectoryGroups(applicationName, directoryGroups);

        final Optional<ApplicationDirectoryGroupsView> existingApplicationDirectoryGroups = applicationDirectoryGroupsQueries.getApplicationDirectoryGroups(applicationName);
        if (existingApplicationDirectoryGroups.isPresent()) {
            // L'utilisateur doit avoir les droits de prod ou appartenir à l'un
            // des groupes AD de l'application pour modifier ses "directory groups"
            if (!user.hasProductionRoleForApplication(applicationName)) {
                throw new UpdateDirectoryGroupsForbiddenException(applicationName);
            }
            applicationDirectoryGroupsCommands.updateApplicationDirectoryGroups(
                    existingApplicationDirectoryGroups.get().getId(),
                    providedApplicationDirectoryGroups,
                    user);
        } else {
            // L'utilisateur doit avoir les droits de prod pour
            // créer les directory groups de cette application
            if (!user.hasProductionRoleForApplication(applicationName)) {
                throw new CreateDirectoryGroupsForbiddenException(applicationName);
            }
            applicationDirectoryGroupsCommands.createApplicationDirectoryGroups(
                    providedApplicationDirectoryGroups,
                    user);
        }
    }

    private Map<String, List<String>> removeDuplicatedCNs(Map<String, List<String>> directoryGroups) {
        return directoryGroups.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().distinct().collect(Collectors.toList())
                ));
    }

    private ApplicationDirectoryGroups validateAndBuildDirectoryGroups(String applicationName, Map<String, List<String>> directoryGroups) {
        ApplicationProdRole applicationProdRole = new ApplicationProdRole(applicationName);
        if (!directoryGroups.containsKey(applicationProdRole.getAuthority()) || directoryGroups.size() != 1) {
            throw new InvalidDirectoryGroupsException("directoryGroups must contain a single key named " + applicationProdRole.getAuthority());
        }
        List<String> directoryGroupCNs = directoryGroups.get(applicationProdRole.getAuthority());

        final DirectoryGroupsView directoryGroupsView = applicationDirectoryGroupsQueries.resolveDirectoryGroupCNs(directoryGroupCNs);
        if (directoryGroupsView.hasUnresolvedOrAmbiguousCNs()) {
            throw new InvalidDirectoryGroupsException(directoryGroupsView.getUnresolvedDirectoryGroupCNs(), directoryGroupsView.getAmbiguousDirectoryGroupCNs());
        }
        return new ApplicationDirectoryGroups(applicationName, directoryGroupsView.getDirectoryGroupDNs());
    }
}
