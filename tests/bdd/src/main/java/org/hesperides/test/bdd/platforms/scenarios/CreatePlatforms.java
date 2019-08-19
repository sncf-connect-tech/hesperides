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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class CreatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;

    public CreatePlatforms() {

        Given("^an existing platform(?: named \"(.*)\")?$", (String platformName) -> {
            platformBuilder.reset();
            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }
            createPlatform();
        });

        Given("^a platform to create" +
                "(?: named \"(.*)\")?" +
                "(?: (?:and|with) version \"(.*)\")?" +
                "( (?:and|with) this module(?: with an empty path)?)?" +
                "( (?:and|with) an instance(?: with properties))?" +
                "( without setting production flag)?$", (
                String platformName,
                String platformVersion,
                String withThisModule,
                String withAnInstance,
                String withoutSettingProductionFlag) -> {

            platformBuilder.reset();

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }
            if (isNotEmpty(platformVersion)) {
                platformBuilder.withVersion(platformVersion);
            }

            if (isNotEmpty(withAnInstance)) {
                instanceBuilder.withValuedProperty("instance-property-a", "instance-property-a-value");
                instanceBuilder.withValuedProperty("instance-property-b", "instance-property-b-value");
                deployedModuleBuilder.withInstanceBuilder(instanceBuilder);
            }

            if (isNotEmpty(withThisModule)) {
                if (withThisModule.contains("with an empty path")) {
                    deployedModuleBuilder.withModulePath("");

                }
                deployedModuleBuilder.fromModuleBuider(moduleBuilder);
                platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            }

            if (isNotEmpty(withoutSettingProductionFlag)) {
                platformBuilder.withIsProductionPlatform(null);
            }
        });

        When("^I( try to)? create this platform$", (String tryTo) -> {
            createPlatform(tryTo);
        });

        Then("^the platform is successfully created(?: and the deployed module has the following path \"(.*)\")?$", (
                String expectedModulePath) -> {
            assertOK();
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            assertEquals(expectedPlatform, actualPlatform);
            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                assertEquals(expectedModulePath, deployedModuleBuilder.getModulePath());
            });
        });

        Then("^the platform creation fails with a conflict error$", this::assertConflict);
    }

    private void createPlatform() {
        createPlatform(null);
        assertOK();
    }

    private void createPlatform(String tryTo) {
        platformClient.createPlatform(platformBuilder.buildInput(), tryTo);
        platformBuilder.incrementVersionId();
        platformBuilder.setDeployedModuleIds();
        platformHistory.addPlatformBuilder(platformBuilder);
    }
}
