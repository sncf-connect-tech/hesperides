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
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
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

    public UpdateProperties() {

        When("^I update the properties of those modules one after the other using the same platform version_id$", () -> {
            Long firstPlatformVersionId = platformBuilder.getVersionId();

            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                platformClient.updateProperties(platformBuilder.buildInput(firstPlatformVersionId), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                platformHistory.updatePlatformBuilder(platformBuilder);
            });
        });

        When("^I update the module properties and then the platform global properties using the same platform version_id$", () -> {
            Long firstPlatformVersionId = platformBuilder.getVersionId();

            platformClient.updateProperties(platformBuilder.buildInput(firstPlatformVersionId), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);

            platformClient.updateGlobalProperties(platformBuilder.buildInput(firstPlatformVersionId), platformBuilder.buildProperties());
            platformBuilder.incrementGlobalPropertiesVersionId();
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        Then("^the properties are successfully updated for those modules$", () -> {

            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                PropertiesIO expectedProperties = deployedModuleBuilder.buildProperties();
                PropertiesIO actualProperties = platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
                assertOK();
                assertEquals(expectedProperties, actualProperties);
            });
        });

        Then("^the platform version_id is also updated$", () -> {
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = platformClient.getPlatform(platformBuilder.buildInput());
            assertOK();
            assertEquals(expectedPlatform, actualPlatform);
        });
    }
}
