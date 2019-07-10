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

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.applications.ApplicationAuthoritiesBuilder;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.extractCN;
import static org.hesperides.test.bdd.users.GetUserInformation.extractAuthoritiesValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetApplications extends HesperidesScenario implements En {

    @Autowired
    private ApplicationClient applicationClient;
    @Autowired
    private ApplicationAuthoritiesBuilder applicationAuthoritiesBuilder;
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

        When("^I get the list of all applications?$", () -> {
            testContext.setResponseEntity(applicationClient.getAllApplications());
        });

        When("^I( try to)? get the applications list$", (String tryTo) -> {
            testContext.setResponseEntity(applicationClient.getApplications(
                    getResponseType(tryTo, SearchResultOutput[].class)));
        });

        When("^I( try to)? get the application details( with parameter hide_platform set to true)?( requesting the passwords count)?$", (
                String tryTo, String withHidePlatform, String requestingThePasswordsCount) -> {
            hidePlatform = StringUtils.isNotEmpty(withHidePlatform);
            final ResponseEntity responseEntity = applicationClient.getApplication(
                    applicationAuthoritiesBuilder.getApplicationName(),
                    hidePlatform,
                    StringUtils.isNotEmpty(requestingThePasswordsCount),
                    getResponseType(tryTo, ApplicationOutput.class));
            testContext.setResponseEntity(responseEntity);
        });

        Then("^all the applications are retrieved with their platforms and their modules$", () -> {
            assertOK();
            List<ApplicationOutput> actualApplications = ((AllApplicationsDetailOutput) testContext.getResponseBody()).getApplications();
            assertEquals(expectedApplications, actualApplications);
        });

        Then("^the application is successfully retrieved", () -> {
            assertOK();
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            assertEquals(expectedApplication, actualApplication);
        });

        Then("^(.+) is listed in the application authorities", (String authority) -> {
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            List<String> authorities = extractAuthoritiesValues((List<Map<String, String>>) actualApplication.getAuthorities());
            if (authority.equals("A_GROUP")) {
                authority = extractCN(authCredentialsConfig.getLambdaUserParentGroupDN());
            }
            assertThat(authorities, hasItems(authority));
        });

        Then("^the platform has at least (\\d+) password$", (Integer count) -> {
            final Integer actualPasswordCount = ((ApplicationOutput) testContext.getResponseBody()).getPasswordCount();
            Assertions.assertThat(actualPasswordCount).isGreaterThanOrEqualTo(count);
        });

        Then("^the application exact authorities are: (.+)", (String groupCNs) -> {
            fail("TODO");
        });

        Then("^the application now has 0 authorities", () -> {
            fail("TODO");
        });

        Then("^the application details contains these authorities", () -> {
            final Map<String, List<String>> expectedAuthorities = applicationAuthoritiesBuilder.getAuthorities();
            final Map<String, List<String>> actualAuthorities = ((ApplicationOutput) testContext.getResponseBody()).getAuthorities();
            assertEquals(expectedAuthorities, actualAuthorities);
        });
    }
}
