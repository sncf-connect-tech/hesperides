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
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
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

        Then("^the application is successfully retrieved$", () -> {
            assertOK();
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = (ApplicationOutput) testContext.getResponseBody();
            assertEquals(expectedApplication, actualApplication);
        });

        Then("^all the applications are retrieved with their platforms and their modules$", () -> {
            assertOK();
            List<ApplicationOutput> actualApplications = Arrays.asList((ApplicationOutput[]) testContext.getResponseBody());

            for (int i = 0; i < expectedApplications.size(); i++) {
                ApplicationOutput expectedApplication = expectedApplications.get(i);
                ApplicationOutput actualApplication = actualApplications.get(i);
                assertEquals(expectedApplication.getName(), actualApplication.getName());

                for (int j = 0; j < expectedApplication.getPlatforms().size(); j++) {
                    PlatformIO expectedPlatform = expectedApplication.getPlatforms().get(j);
                    PlatformIO actualPlatform = actualApplication.getPlatforms().get(j);
                    assertEquals(expectedPlatform.getApplicationName(), actualPlatform.getApplicationName());
                    assertEquals(expectedPlatform.getPlatformName(), actualPlatform.getPlatformName());
                    assertEquals(expectedPlatform.getIsProductionPlatform(), actualPlatform.getIsProductionPlatform());

                    for (int k = 0; k < expectedPlatform.getDeployedModules().size(); k++) {
                        DeployedModuleIO expectedModule = expectedPlatform.getDeployedModules().get(k);
                        DeployedModuleIO actualModule = actualPlatform.getDeployedModules().get(k);
                        assertEquals(expectedModule.getName(), actualModule.getName());
                        assertEquals(expectedModule.getVersion(), actualModule.getVersion());
                        assertEquals(expectedModule.getIsWorkingCopy(), actualModule.getIsWorkingCopy());
                    }
                }
            }
        });
    }
}
