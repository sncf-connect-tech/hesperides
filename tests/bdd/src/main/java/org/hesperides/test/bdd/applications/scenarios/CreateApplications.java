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

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.applications.ApplicationDirectoryGroupsBuilder;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

public class CreateApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ApplicationClient applicationClient;
    @Autowired
    private ApplicationDirectoryGroupsBuilder applicationDirectoryGroupsBuilder;

    public CreateApplications() {

        Given("^an application without directory groups", () -> {
            fail("TODO");
        });

        Given("^an application ?(.+)? associated with the following directory groups", (String applicationName, DataTable data) -> {
            final List<String> directoryGroups = data.asList(String.class);
            createPlatformAndSetApplicationDirectoryGroups(applicationName, directoryGroups);
        });

        Given("^an application ?(.+)? associated with the given directory group$", (String applicationName) -> {
            final String givenDirectoryGroup = authCredentialsConfig.getLambdaParentGroupDN();
            createPlatformAndSetApplicationDirectoryGroups(applicationName, Collections.singletonList(givenDirectoryGroup));
        });
    }

    private void createPlatformAndSetApplicationDirectoryGroups(String applicationName, List<String> directoryGroups) {
        if (StringUtils.isNotEmpty(applicationName)) {
            platformBuilder.withApplicationName(applicationName);
        }
        platformClient.create(platformBuilder.buildInput());
        applicationDirectoryGroupsBuilder.withApplicationName(platformBuilder.getApplicationName());
        applicationDirectoryGroupsBuilder.withDirectoryGroups(directoryGroups);
        applicationClient.setApplicationDirectoryGroups(
                applicationDirectoryGroupsBuilder.getApplicationName(),
                applicationDirectoryGroupsBuilder.buildInput());
    }
}
