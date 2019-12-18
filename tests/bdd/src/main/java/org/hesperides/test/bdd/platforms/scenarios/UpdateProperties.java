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
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class UpdateProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private SaveProperties saveProperties;

    public UpdateProperties() {

        When("^I update the properties(?: with the comment \"([^\"]*)\")?$", (String comment) -> {
            deployedModuleBuilder.withValuedProperty("new-property", "new-value");
            platformClient.updateProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(),
                    deployedModuleBuilder.buildPropertiesPath(), comment, null);
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        When("^I update the properties of those modules one after the other using the same platform version_id$", () -> {
            Long firstPlatformVersionId = platformBuilder.getVersionId();

            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                platformClient.updateProperties(platformBuilder.buildInputWithPlatformVersionId(firstPlatformVersionId),
                        deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                platformHistory.updatePlatformBuilder(platformBuilder);
            });
        });

        When("^I update the module properties and then the platform global properties using the same platform version_id$", () -> {
            Long firstPlatformVersionId = platformBuilder.getVersionId();

            platformClient.updateProperties(platformBuilder.buildInputWithPlatformVersionId(firstPlatformVersionId), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);

            platformClient.updateGlobalProperties(platformBuilder.buildInputWithPlatformVersionId(firstPlatformVersionId), platformBuilder.buildProperties());
            platformBuilder.incrementGlobalPropertiesVersionId();
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        When("^I try to update the module properties and then the platform using the same platform version_id$", () -> {
            Long firstPlatformVersionId = platformBuilder.getVersionId();
            platformClient.updateProperties(platformBuilder.buildInputWithPlatformVersionId(firstPlatformVersionId), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformClient.updatePlatform(platformBuilder.buildInputWithPlatformVersionId(firstPlatformVersionId), false, "should-fail");
        });

        When("^I try to update the properties of this module twice with the same properties version_id$", () -> {
            Long firstPropertiesVersionId = deployedModuleBuilder.getPropertiesVersionId();
            platformClient.updateProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(firstPropertiesVersionId), deployedModuleBuilder.buildPropertiesPath());
            platformClient.updateProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(firstPropertiesVersionId), deployedModuleBuilder.buildPropertiesPath(), null, "should-fail");
        });

        When("^I try to update global properties twice with the same global properties version_id$", () -> {
            Long firstGlobalPropertiesVersionId = platformBuilder.getGlobalPropertiesVersionId();
            platformClient.updateGlobalProperties(platformBuilder.buildInput(), platformBuilder.buildProperties(firstGlobalPropertiesVersionId));
            platformClient.updateGlobalProperties(platformBuilder.buildInput(), platformBuilder.buildProperties(firstGlobalPropertiesVersionId), "should-fail");
        });

        When("^I update the properties with wrong platform_version_id$", () -> {
            platformClient.updateProperties(platformBuilder.buildInputWithPlatformVersionId(123L), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        Then("^the properties are successfully updated for those modules$", () ->
                platformBuilder.getDeployedModuleBuilders().forEach(saveProperties::assertValuedProperties));

        Then("^the platform version_id is also updated$", () -> {
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = platformClient.getPlatform(platformBuilder.buildInput());
            assertOK();
            assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the properties versionId should stay the same$", () -> {
            Long expectedPropertiesVersionId = deployedModuleBuilder.getPropertiesVersionId();
            PlatformIO actualPlatform = testContext.getResponseBody();
            Long actualPropertiesVersionId = actualPlatform.getDeployedModules().get(0).getPropertiesVersionId();
            assertEquals(expectedPropertiesVersionId, actualPropertiesVersionId);
        });
    }
}
