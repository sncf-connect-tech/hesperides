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
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;

public class CopyPlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public CopyPlatforms() {

        When("^I( try to)? copy this platform( using the same key)?$", (String tryTo, String usingTheSameKey) -> {

            PlatformIO existingPlatform = platformBuilder.buildInput();

            if (isEmpty(usingTheSameKey)) {
                platformBuilder.withPlatformName(existingPlatform.getPlatformName() + "-copy");
            }

            platformClient.copyPlatform(existingPlatform, platformBuilder.buildInput(), false, tryTo);
            if (isEmpty(tryTo)) {
                platformBuilder.withVersionId(1);
                platformBuilder.withGlobalPropertyVersionId(0);
                platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> deployedModuleBuilder.withPropertiesVersionId(1));
                platformHistory.addPlatformBuilder(platformBuilder);
            }
        });

        Then("^the platform property values are(?: also)? copied$", () -> {
            // Propriétés valorisées au niveau des modules
            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                PropertiesIO expectedModuleProperties = deployedModuleBuilder.buildProperties();
                platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
                PropertiesIO actualModuleProperties = testContext.getResponseBody(PropertiesIO.class);
                assertEquals(expectedModuleProperties, actualModuleProperties);
            });
            // Propriétés globales
            PropertiesIO expectedGlobalProperties = platformBuilder.buildProperties();
            platformClient.getGlobalProperties(platformBuilder.buildInput());
            PropertiesIO actualGlobalProperties = testContext.getResponseBody(PropertiesIO.class);
            assertEquals(expectedGlobalProperties, actualGlobalProperties);

//            assertThat(actualProperties.getValuedProperties(), containsInAnyOrder(expectedProperties.getValuedProperties().toArray()));
        });

        Then("^the platform copy fails with a not found error$", this::assertNotFound);

        Then("^the platform copy fails with a conflict error$", this::assertConflict);
    }
}
