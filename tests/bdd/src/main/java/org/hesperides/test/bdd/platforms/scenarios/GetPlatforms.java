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
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class GetPlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    public GetPlatforms() {

        When("^I( try to)? get the platform(?: \"(.*)\")? detail" +
                "( requesting the password flag)?$", (
                String tryTo,
                String platformName,
                String withPasswordFlag) -> {

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }

            platformClient.getPlatform(platformBuilder.buildInput(), null, isNotEmpty(withPasswordFlag), tryTo);
        });

        Then("^the platform detail is successfully retrieved", () -> {
            assertOK();
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform has the password flag and the flag is set to (true|false)?$", (String trueOrFalse) -> {
            Boolean hasPasswords = testContext.getResponseBody(PlatformIO.class).getHasPasswords();
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
    }
}
