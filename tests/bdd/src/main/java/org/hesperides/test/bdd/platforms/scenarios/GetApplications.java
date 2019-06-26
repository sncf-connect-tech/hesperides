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
package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GetApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private List<ApplicationOutput> expectedApplications;

    private boolean hidePlatform;

    public GetApplications() {

        Given("^a list of applications with platforms and modules?$", () -> {
            final DeployedModuleIO deployedModuleIO = new DeployedModuleIO(1L, "name", "version", true, "#path", "", Collections.emptyList());

            Arrays.asList("ABC", "DEF", "GHI").forEach(applicationName -> {
                final PlatformIO platform = new PlatformIO("DEV", applicationName, "1", true, Arrays.asList(deployedModuleIO), 1L);
                platformClient.create(platform);
                expectedApplications.add(new ApplicationOutput(applicationName, Arrays.asList(platform)));
            });

        });

        When("^I( try to)? get the platform application( with parameter hide_platform set to true)?$", (String tryTo, String withHidePlatform) -> {
            hidePlatform = StringUtils.isNotEmpty(withHidePlatform);
            testContext.responseEntity = platformClient.getApplication(
                    platformBuilder.buildInput(),
                    hidePlatform,
                    getResponseType(tryTo, ApplicationOutput.class));
        });

        When("^I get the list of all applications?$", () -> {
            testContext.responseEntity = platformClient.getAllApplications();
        });

        Then("^the application is successfully retrieved", () -> {
            assertOK();
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            Assert.assertEquals(expectedApplication, actualApplication);
        });

        Then("^all the applications are retrieved with their platforms and their modules", () -> {
            assertOK();
            Assert.assertEquals(expectedApplications, testContext.getResponseBody());
        });
    }
}
