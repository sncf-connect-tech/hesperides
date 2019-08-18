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
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
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
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    private List<ApplicationOutput> expectedApplications = new ArrayList<>();

    private boolean hidePlatform;

    public GetApplications() {

        Given("^a list of applications with platforms and this module$", () -> {
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            Arrays.asList("ABC", "DEF", "GHI").forEach(applicationName -> {
                oldPlatformBuilder.withApplicationName(applicationName);
                oldPlatformClient.create(oldPlatformBuilder.buildInput());
                expectedApplications.add(oldPlatformBuilder.buildApplicationOutput(false));
            });

        });

        When("^I get all the applications detail( requesting the password flag)?$", (String requestingThePasswordFlag) -> {
            testContext.setResponseEntity(applicationClient.getAllApplicationsDetail(StringUtils.isNotEmpty(requestingThePasswordFlag)));
        });

        When("^I( try to)? get the applications name", (String tryTo) -> {
            testContext.setResponseEntity(applicationClient.getApplications(
                    getResponseType(tryTo, SearchResultOutput[].class)));
        });

        When("^I( try to)? get the application detail( with parameter hide_platform set to true)?( requesting the password flag)?$", (
                String tryTo, String withHidePlatform, String requestingThePasswordFlag) -> {
            hidePlatform = StringUtils.isNotEmpty(withHidePlatform);
            final ResponseEntity responseEntity = applicationClient.getApplication(
                    applicationDirectoryGroupsBuilder.getApplicationName(),
                    hidePlatform,
                    StringUtils.isNotEmpty(requestingThePasswordFlag),
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
            ApplicationOutput expectedApplication = oldPlatformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = testContext.getResponseBody(ApplicationOutput.class);
            assertEquals(expectedApplication, actualApplication);
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

        Then("^the application platform has the password flag$", () -> {
            Boolean hasPasswords = testContext.getResponseBody(ApplicationOutput.class).getPlatforms().get(0).getHasPasswords();
            Assertions.assertThat(hasPasswords).isNotNull();
        });

        Then("^the applications platforms have the password flag$", () -> {
            Boolean hasPasswords = testContext.getResponseBody(AllApplicationsDetailOutput.class).getApplications().get(0).getPlatforms().get(0).getHasPasswords();
            Assertions.assertThat(hasPasswords).isNotNull();
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
