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
package org.hesperides.core.domain.security.queries.views;

import lombok.Value;
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups;
import org.hesperides.core.domain.security.entities.authorities.ApplicationProdRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups.getCnFromDn;

@Value
public class ApplicationDirectoryGroupsView {
    String id;
    String applicationName;
    Map<String, List<String>> directoryGroups;

    public ApplicationDirectoryGroupsView(String id, String applicationName, List<String> directoryGroupDNs) {
        this.id = id;
        this.applicationName = applicationName;
        this.directoryGroups = new HashMap<>();
        ApplicationProdRole applicationProdRole = new ApplicationProdRole(applicationName);
        this.directoryGroups.put(applicationProdRole.getAuthority(), directoryGroupDNs);
    }

    public Map<String, List<String>> getDirectoryGroupCNs() {
        return directoryGroups.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(ApplicationDirectoryGroups::extractCnFromDn)
                                .filter(Optional::isPresent).map(Optional::get)
                                .collect(Collectors.toList())
                ));
    }
}
