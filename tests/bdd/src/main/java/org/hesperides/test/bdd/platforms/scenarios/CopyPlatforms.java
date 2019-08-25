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
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CopyPlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public CopyPlatforms() {

        When("^I( try to)? copy this platform( using the same key)?( without copying instances or properties)?$", (
                String tryTo, String usingTheSameKey, String withoutInstancesOrProperties) -> {

            PlatformIO existingPlatform = platformBuilder.buildInput();

            if (isEmpty(usingTheSameKey)) {
                platformBuilder.withPlatformName(existingPlatform.getPlatformName() + "-copy");
            }

            platformClient.copyPlatform(existingPlatform, platformBuilder.buildInput(), isNotEmpty(withoutInstancesOrProperties), tryTo);

            if (isEmpty(tryTo)) {
                platformBuilder.withVersionId(1);
                platformBuilder.withGlobalPropertyVersionId(0);

                if (isNotEmpty(withoutInstancesOrProperties)) {
                    DeployedModuleBuilder.initPropertiesVersionIdTo(0, platformBuilder.getDeployedModuleBuilders());
                    platformBuilder.clearGlobalProperties();
                    DeployedModuleBuilder.clearInstancesAndProperties(platformBuilder.getDeployedModuleBuilders());
                }

                platformHistory.addPlatformBuilder(platformBuilder);
            }
        });

        Then("^the platform property values are(?: also)? copied$", () -> {
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

        Then("^the platform property values are not copied$", () -> {
            platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                PropertiesIO expectedModuleProperties = new PropertiesIO(1L, Collections.emptySet(), Collections.emptySet());
                PropertiesIO actualModuleProperties = platformClient.getProperties(
                        platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
                assertEquals(expectedModuleProperties, actualModuleProperties);
            });
        });

        Then("^the new platform has one module, no instances, no global properties and no module properties$", () -> {
            PlatformIO copiedPlatform = testContext.getResponseBody();
            assertThat(copiedPlatform.getDeployedModules(), hasSize(1));

            DeployedModuleIO deployedModule = copiedPlatform.getDeployedModules().get(0);
            assertThat(deployedModule.getInstances(), hasSize(0));

            PropertiesIO globalProperties = platformClient.getProperties(copiedPlatform, "#");
            assertThat(globalProperties.getValuedProperties(), hasSize(0));

            PropertiesIO moduleProperties = platformClient.getProperties(copiedPlatform, deployedModule.getPropertiesPath());
            assertThat(moduleProperties.getValuedProperties(), hasSize(0));
            assertThat(moduleProperties.getIterableValuedProperties(), hasSize(0));
        });

        Then("^the platform copy fails with a not found error$", this::assertNotFound);

        Then("^the platform copy fails with a conflict error$", this::assertConflict);
    }
}
