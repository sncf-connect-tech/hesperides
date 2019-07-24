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
package org.hesperides.test.bdd.applications;

import org.hesperides.core.presentation.io.platforms.ApplicationDirectoryGroupsInput;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApplicationDirectoryGroupsBuilder {

    private String applicationName;
    private Map<String, List<String>> directoryGroups;

    public ApplicationDirectoryGroupsBuilder() {
        reset();
    }

    public ApplicationDirectoryGroupsBuilder reset() {
        applicationName = "test-application";
        directoryGroups = new HashMap<>();
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void withApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void addDirectoryGroups(List<String> providedDirectoryGroupCNs) {
        if (directoryGroups.containsKey(getDirectoryGroupsKey())) {
            List<String> existingDirectoryGroupCNs = directoryGroups.get(getDirectoryGroupsKey());
            List<String> newDirectoryGroupCNs = new ArrayList<>();
            newDirectoryGroupCNs.addAll(existingDirectoryGroupCNs);
            newDirectoryGroupCNs.addAll(providedDirectoryGroupCNs);
            directoryGroups.put(getDirectoryGroupsKey(), newDirectoryGroupCNs);
        } else {
            directoryGroups.put(getDirectoryGroupsKey(), providedDirectoryGroupCNs);
        }
    }

    public String getDirectoryGroupsKey() {
        return applicationName + "_PROD_USER";
    }

    public ApplicationDirectoryGroupsInput buildInput() {
        return new ApplicationDirectoryGroupsInput(directoryGroups);
    }

    public Map<String, List<String>> getDirectoryGroups(List<String> directoryGroupCNs) {
        Map<String, List<String>> directoryGroups = new HashMap<>();
        directoryGroups.put(getDirectoryGroupsKey(), directoryGroupCNs);
        return directoryGroups;
    }

    public void removeDirectoryGroups() {
        directoryGroups.put(getDirectoryGroupsKey(), Collections.emptyList());
    }
}
