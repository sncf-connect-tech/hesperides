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
import org.hesperides.core.domain.security.entities.springauthorities.ApplicationProdRole;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class ApplicationDirectoryGroupsView {
    String id;
    String applicationName;
    Map<String, List<String>> directoryGroupDNs;

    public ApplicationDirectoryGroupsView(String id, String applicationName, List<String> directoryGroupDNs) {
        this.id = id;
        this.applicationName = applicationName;
        this.directoryGroupDNs = directoryGroupsMapFromAppNameAndList(applicationName, directoryGroupDNs);
    }

    public static Map<String, List<String>> directoryGroupsMapFromAppNameAndList(String appName, List<String> directoryGroups) {
        Map<String, List<String>> directoryGroupsMap = new HashMap<>();
        ApplicationProdRole applicationProdRole = new ApplicationProdRole(appName);
        directoryGroupsMap.put(applicationProdRole.getAuthority(), directoryGroups);
        return directoryGroupsMap;
    }

    public Map<String, List<String>> getDirectoryGroupCNs() {
        return directoryGroupDNs.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(DirectoryGroupDN::extractCnFromDn)
                                .collect(Collectors.toList())
                ));
    }
}
