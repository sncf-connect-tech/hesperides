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
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.applications.ApplicationBuilder;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.extractCN;
import static org.hesperides.test.bdd.users.GetUserInformation.extractAuthoritiesValues;
import static org.junit.Assert.assertThat;

public class GetApplications extends HesperidesScenario implements En {

    @Autowired
    private ApplicationClient applicationClient;
    @Autowired
    private ApplicationBuilder applicationBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;

    private boolean hidePlatform;

    public GetApplications() {

        When("^I( try to)? get the applications list$", (String tryTo) -> {
            testContext.setResponseEntity(applicationClient.getApplications(
                    getResponseType(tryTo, SearchResultOutput[].class)));
        });

        When("^I( try to)? get the application details( with parameter hide_platform set to true)?( requesting the passwords count)?$", (String tryTo, String withHidePlatform, String requestingThePasswordsCount) -> {
            assertNull("TODO", requestingThePasswordsCount);
            hidePlatform = StringUtils.isNotEmpty(withHidePlatform);
            testContext.setResponseEntity(applicationClient.getApplication(
                    applicationBuilder.getApplicationName(),
                    hidePlatform,
                    getResponseType(tryTo, ApplicationOutput.class)));
        });

        Then("^the application is successfully retrieved", () -> {
            assertOK();
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            Assert.assertEquals(expectedApplication, actualApplication);
        });

        Then("^(.+) is listed in the application authorities", (String authority) -> {
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            List<String> authorities = extractAuthoritiesValues((List<Map<String, String>>)actualApplication.getAuthorities());
            if (authority.equals("A_GROUP")) {
                authority = extractCN(authCredentialsConfig.getLambdaUserParentGroupDN());
            }
            assertThat(authorities, hasItems(authority));
        });

        Then("^the password count of the platform is greater than (\\d+)", (Integer count) -> {
            fail("TODO");
        });

        Then("^the application exact authorities are: (.+)", (String groupCNs) -> {
            fail("TODO");
        });

        Then("^the application now has 0 authorities", () -> {
            fail("TODO");
        });
    }
}
