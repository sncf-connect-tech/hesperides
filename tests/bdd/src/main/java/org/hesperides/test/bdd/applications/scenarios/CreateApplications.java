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
package org.hesperides.test.bdd.applications.scenarios;

import io.cucumber.java.en.Given;
import io.cucumber.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.applications.ApplicationDirectoryGroupsBuilder;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.configuration.AuthorizationCredentialsConfig;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.platforms.scenarios.CreatePlatforms;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CreateApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ApplicationClient applicationClient;
    @Autowired
    private ApplicationDirectoryGroupsBuilder applicationDirectoryGroupsBuilder;
    @Autowired
    private AuthorizationCredentialsConfig authorizationCredentialsConfig;
    @Autowired
    private CreatePlatforms createPlatforms;

    public CreateApplications() {

        Given("^an application without directory groups", () -> {
            createApplication(null);
            assertOK();
        });

        Given("^an application ?(.+)? associated with the directory group (.+)$", (String applicationName, String directoryGroup) -> {
            createApplication(applicationName);
            assertOK();
            String realDirectoryGroup = authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup);
            addApplicationDirectoryGroups(realDirectoryGroup);
            assertOK();
        });

        Given("^its application is associated with the directory group (.+)$", (String directoryGroup) -> {
            String realDirectoryGroup = authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup);
            addApplicationDirectoryGroups(realDirectoryGroup);
            assertOK();
        });

        When("^I(?: try to)? add (.*)? directory group to the application$", (String directoryGroup) -> {
            String realDirectoryGroup = authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup);
            addApplicationDirectoryGroups(realDirectoryGroup);
        });

        When("^I remove all directory groups on the application$", () -> {
            applicationDirectoryGroupsBuilder.removeDirectoryGroups();
            applicationClient.setApplicationDirectoryGroups(
                    applicationDirectoryGroupsBuilder.getApplicationName(),
                    applicationDirectoryGroupsBuilder.buildInput());
            assertOK();
        });
    }

    @Given("^an application ?(.+)? associated with the directory groups$")
    public void anAppAssociatedWithTheseDirectoryGroups(String applicationName, List<String> directoryGroups) {
        createApplication(applicationName);
        assertOK();
        List<String> realDirectoryGroups = directoryGroups.stream().map(directoryGroup ->
                authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup)).collect(Collectors.toList());
        addApplicationDirectoryGroups(realDirectoryGroups);
        assertOK();
    }

    private void createApplication(String applicationName) {
        if (StringUtils.isNotEmpty(applicationName)) {
            platformBuilder.withApplicationName(applicationName);
        }
        createPlatforms.createPlatform();
    }

    private void addApplicationDirectoryGroups(String directoryGroup) {
        addApplicationDirectoryGroups(Collections.singletonList(directoryGroup));
    }

    private void addApplicationDirectoryGroups(List<String> directoryGroups) {
        applicationDirectoryGroupsBuilder.withApplicationName(platformBuilder.getApplicationName());
        applicationDirectoryGroupsBuilder.addDirectoryGroups(directoryGroups);
        applicationClient.setApplicationDirectoryGroups(
                applicationDirectoryGroupsBuilder.getApplicationName(),
                applicationDirectoryGroupsBuilder.buildInput());
    }
}
