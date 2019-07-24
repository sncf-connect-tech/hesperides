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
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.applications.ApplicationDirectoryGroupsBuilder;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class GetApplications extends HesperidesScenario implements En {

    @Autowired
    private ApplicationClient applicationClient;
    @Autowired
    private ApplicationDirectoryGroupsBuilder applicationDirectoryGroupsBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private List<ApplicationOutput> expectedApplications = new ArrayList<>();

    private boolean hidePlatform;

    public GetApplications() {

        Given("^a list of applications with platforms and this module$", () -> {
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            Arrays.asList("ABC", "DEF", "GHI").forEach(applicationName -> {
                platformBuilder.withApplicationName(applicationName);
                platformClient.create(platformBuilder.buildInput());
                expectedApplications.add(platformBuilder.buildApplicationOutput(false));
            });

        });

        When("^I get all the applications detail$", () -> {
            testContext.setResponseEntity(applicationClient.getAllApplicationsDetail());
        });

        When("^I( try to)? get the applications name", (String tryTo) -> {
            testContext.setResponseEntity(applicationClient.getApplications(
                    getResponseType(tryTo, SearchResultOutput[].class)));
        });

        When("^I( try to)? get the application detail( with parameter hide_platform set to true)?( requesting the passwords count)?$", (
                String tryTo, String withHidePlatform, String requestingThePasswordsCount) -> {
            hidePlatform = StringUtils.isNotEmpty(withHidePlatform);
            final ResponseEntity responseEntity = applicationClient.getApplication(
                    applicationDirectoryGroupsBuilder.getApplicationName(),
                    hidePlatform,
                    StringUtils.isNotEmpty(requestingThePasswordsCount),
                    getResponseType(tryTo, ApplicationOutput.class));
            testContext.setResponseEntity(responseEntity);
        });

        Then("^all the applications are retrieved with their platforms and their modules$", () -> {
            assertOK();
            List<ApplicationOutput> actualApplications = testContext.getResponseBody(AllApplicationsDetailOutput.class).getApplications();
            assertEquals(expectedApplications, actualApplications);
        });

        Then("^the application is successfully retrieved", () -> {
            assertOK();
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = testContext.getResponseBody(ApplicationOutput.class);
            assertEquals(expectedApplication, actualApplication);
        });

        Then("^the platform has at least (\\d+) password$", (Integer count) -> {
            final Integer actualPasswordCount = testContext.getResponseBody(ApplicationOutput.class).getPasswordCount();
            Assertions.assertThat(actualPasswordCount).isGreaterThanOrEqualTo(count);
        });

        Then("^the application details contains the directory group (.*)?", (String directoryGroupCN) -> {
            assertDirectoryGroups(Collections.singletonList(directoryGroupCN));
        });

        Then("^the application details contains the directory groups", (DataTable directoryGroupCNs) -> {
            assertDirectoryGroups(directoryGroupCNs.asList(String.class));
        });

        Then("^the application details contains no directory groups", () -> {
            final String directoryGroupsKey = applicationDirectoryGroupsBuilder.getDirectoryGroupsKey();
            final List<String> actualDirectoryGroups = testContext.getResponseBody(ApplicationOutput.class).getDirectoryGroups().get(directoryGroupsKey);
            assertThat(actualDirectoryGroups).isEmpty();
        });
    }

    private void assertDirectoryGroups(List<String> directoryGroupCNs) {
        List<String> realDirectoryGroupCNs = directoryGroupCNs.stream().map(directoryGroupCN ->
                authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroupCN)).collect(Collectors.toList());
        final Map<String, List<String>> expectedDirectoryGroups = applicationDirectoryGroupsBuilder.getDirectoryGroups(realDirectoryGroupCNs);
        final Map<String, List<String>> actualDirectoryGroups = testContext.getResponseBody(ApplicationOutput.class).getDirectoryGroups();
        assertEquals(expectedDirectoryGroups, actualDirectoryGroups);
    }
}
