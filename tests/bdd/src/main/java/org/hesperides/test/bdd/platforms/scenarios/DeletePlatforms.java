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
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class DeletePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public DeletePlatforms() {

        When("^I( try to)? delete( and restore)? this platform( with the wrong letter case)?$", (
                String tryTo, String restorePlatform, String wrongCase) -> {
            platformClient.deletePlatform(platformBuilder.buildInput(), tryTo);
            if (isEmpty(tryTo) && isEmpty(restorePlatform)) {
                platformHistory.removePlatformBuilder(platformBuilder);
                //platformBuilder.reset();
            }
            if (isNotEmpty(restorePlatform)) {
                assertOK(); // On vérifie d'abord que la suppression s'est bien déroulée
                if (isNotEmpty(wrongCase)) {
                    platformBuilder.withPlatformName(platformBuilder.getPlatformName().toUpperCase());
                }
                platformClient.restorePlatform(platformBuilder.buildInput(), tryTo);
            }
        });

        When("^I try to restore this platform$", () -> {
            platformClient.restorePlatform(platformBuilder.buildInput(), "should-fail");
        });

        Then("^the platform is successfully deleted", () -> {
            assertOK();
            platformClient.getPlatform(platformBuilder.buildInput(), null, false, "should-fail");
            assertNotFound();
        });

        Then("^the platform is successfully restored with its properties and everything", () -> {
            //Factoriser dans GetPlatforms
            assertOK();
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);

            //todo à bouger dans GetProperties
            // Propriétés valorisées au niveau des modules
            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                PropertiesIO expectedModuleProperties = deployedModuleBuilder.buildProperties();
                PropertiesIO actualModuleProperties = platformClient.getProperties(
                        platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
                assertEquals(expectedModuleProperties, actualModuleProperties);
            });
            // Propriétés globales
            PropertiesIO expectedGlobalProperties = platformBuilder.buildProperties();
            PropertiesIO actualGlobalProperties = platformClient.getGlobalProperties(platformBuilder.buildInput());
            assertEquals(expectedGlobalProperties, actualGlobalProperties);
        });

        Then("^the platform deletion is rejected with a not found error$", this::assertNotFound);
    }
}
