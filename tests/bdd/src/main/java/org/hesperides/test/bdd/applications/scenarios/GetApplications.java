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
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.applications.ApplicationDirectoryGroupsBuilder;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.platforms.scenarios.CreatePlatforms;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private CreatePlatforms createPlatforms;

    public GetApplications() {

        Given("^a list of applications with platforms and this module$", () -> {
            deployedModuleBuilder.fromModuleBuider(moduleBuilder);
            platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            Arrays.asList("ABC", "DEF", "GHI").forEach(applicationName -> {
                platformBuilder.withVersionId(0);
                platformBuilder.withApplicationName(applicationName);
                createPlatforms.createPlatform();
            });
        });

        When("^I get all the applications detail( requesting the password flag)?$", (String requestingThePasswordFlag) -> {
            applicationClient.getAllApplicationsDetail(isNotEmpty(requestingThePasswordFlag));
        });

        When("^I( try to)? get the applications name", (String tryTo) -> applicationClient.getApplications(tryTo));

        When("^I( try to)? get the application detail" +
                "( without the platform modules)?" +
                "( requesting the password flag)?$", (
                String tryTo,
                String withoutPlatformModules,
                String requestingThePasswordFlag) -> {

            applicationClient.getApplication(
                    applicationDirectoryGroupsBuilder.getApplicationName(),
                    isNotEmpty(withoutPlatformModules),
                    isNotEmpty(requestingThePasswordFlag),
                    tryTo);
        });

        Then("^all the applications are retrieved with their platforms and their modules$", () -> {
            assertOK();
            List<ApplicationOutput> expectedApplications = platformHistory.buildApplicationOutputs();
            List<ApplicationOutput> actualApplications = testContext.getResponseBody(AllApplicationsDetailOutput.class).getApplications();
            assertEquals(expectedApplications, actualApplications);
        });

        Then("^the application is successfully retrieved( without the platform modules)?", (String withoutPlatformModules) -> {
            assertOK();
            ApplicationOutput expectedApplication = platformHistory.buildApplicationOutput(isNotEmpty(withoutPlatformModules));
            ApplicationOutput actualApplication = testContext.getResponseBody();
            assertEquals(expectedApplication, actualApplication);
        });

        Then("^the application details contains the directory group (.*)?", (String directoryGroupCN) -> {
            assertDirectoryGroups(Collections.singletonList(directoryGroupCN));
        });

        Then("^the application details contains the directory groups", (DataTable directoryGroupCNs) -> {
            assertDirectoryGroups(directoryGroupCNs.asList(String.class));
        });

        Then("^the application details contains no directory groups", () -> {
            String directoryGroupsKey = applicationDirectoryGroupsBuilder.getDirectoryGroupsKey();
            List<String> actualDirectoryGroups = testContext.getResponseBody(ApplicationOutput.class).getDirectoryGroups().get(directoryGroupsKey);
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
