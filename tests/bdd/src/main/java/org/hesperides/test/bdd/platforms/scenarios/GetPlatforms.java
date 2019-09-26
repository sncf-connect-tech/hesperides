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
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class GetPlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private CreatePlatforms createPlatforms;

    public GetPlatforms() {

        When("^I( try to)? get the platform(?: \"([^\"]*)\")? detail" +
                "( at a specific time in the past)?" +
                "( at the time of the EPOCH)?" +
                "( with the wrong letter case)?" +
                "( requesting the password flag)?$", (
                String tryTo,
                String platformName,
                String withTimestamp,
                String withEpochTimestamp,
                String wrongLetterCase,
                String withPasswordFlag) -> {

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }

            Long timestamp = null;
            if (isNotEmpty(withTimestamp)) {
                timestamp = platformHistory.getPlatformFirstTimestamp(platformBuilder.getApplicationName(), platformBuilder.getPlatformName());
            } else if (isNotEmpty(withEpochTimestamp)) {
                timestamp = 0L;
            }

            platformName = isNotEmpty(wrongLetterCase) ? platformBuilder.getPlatformName().toUpperCase() : platformBuilder.getPlatformName();
            platformClient.getPlatform(platformBuilder.buildInputWithPlatformName(platformName), timestamp, isNotEmpty(withPasswordFlag), tryTo);
        });

        Then("^the( initial)? platform detail is successfully retrieved", (String initialPlatform) -> {
            createPlatforms.assertPlatform(isNotEmpty(initialPlatform) ? platformHistory.getFirstPlatformBuilder(
                    platformBuilder.getApplicationName(), platformBuilder.getPlatformName()) : platformBuilder);
        });

        Then("^the platform has the password flag and the flag is set to (true|false)?$", (String trueOrFalse) -> {
            PlatformIO platform = testContext.getResponseBody();
            Boolean hasPasswords = platform.getHasPasswords();
            assertThat(hasPasswords).isNotNull();
            assertEquals("true".equals(trueOrFalse), hasPasswords);
        });

        Then("^the platform has (\\d+) modules?$", (String expectedNumberOfModules) -> {
            PlatformIO actualPlatform = testContext.getResponseBody();
            Assert.assertThat(actualPlatform.getDeployedModules(), hasSize(Integer.parseInt(expectedNumberOfModules)));
        });

        Then("^the platform has (\\d+) global properties?$", (String expectedNumberOfGlobalProperties) -> {
            PropertiesIO globalProperties = platformClient.getGlobalProperties(platformBuilder.buildInput());
            Assert.assertThat(globalProperties.getValuedProperties(), hasSize(Integer.parseInt(expectedNumberOfGlobalProperties)));
        });

        // Get platforms using module

        When("^I get the platforms using this module$", () -> {
            platformClient.getPlatformsUsingModule(moduleBuilder.build());
        });

        Then("^the platforms using this module are successfully retrieved", () -> {
            assertOK();
            List<ModulePlatformsOutput> expectedPlatforms = platformHistory.buildModulePlatforms(deployedModuleBuilder);
            List<ModulePlatformsOutput> actualPlatforms = testContext.getResponseBodyAsList();
            assertEquals(expectedPlatforms, actualPlatforms);
        });

        Then("^(\\d+) platforms? (?:are|is) retrieved", (Integer count) -> {
            assertOK();
            List<ModulePlatformsOutput> actualPlatforms = testContext.getResponseBodyAsList();
            Assert.assertThat(actualPlatforms, hasSize(count));
        });
    }
}
