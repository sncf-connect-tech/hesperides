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
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class GetProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetProperties() {

        When("^I( try to)? get the platform properties for this module( with an invalid version type)?$", (
                String tryTo, String invalidVersionType) -> {

            if (isNotEmpty(invalidVersionType)) {
                deployedModuleBuilder.withVersionType("TOTO");
            }

            platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath(), null, tryTo);
        });

        When("^I get the global properties of this platform$", () -> {
            platformClient.getGlobalProperties(platformBuilder.buildInput());
        });

        When("^the platform( global)? properties are successfully retrieved$", (String globalProperties) -> {
            assertOK();
            PropertiesIO expectedModuleProperties = isNotEmpty(globalProperties)
                    ? platformBuilder.buildProperties() : deployedModuleBuilder.buildProperties();
            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            assertEquals(expectedModuleProperties, actualModuleProperties);
        });

        When("^I get this platform global properties usage$", () -> {
            platformClient.getGlobalPropertiesUsage(platformBuilder.buildInput());
        });

        Then("^the platform global properties usage is successfully retrieved$", () -> {
            assertOK();
            Map<String, Set<GlobalPropertyUsageOutput>> expectedGlobalPropertiesUsage = platformBuilder.buildGlobalPropertiesUsage(moduleHistory);
            Map<String, Set<GlobalPropertyUsageOutput>> actualGlobalPropertiesUsage = testContext.getResponseBody();
            assertEquals(expectedGlobalPropertiesUsage, actualGlobalPropertiesUsage);
        });

        Then("^the password property values are obfuscated$", () -> {
            assertOK();
            PropertiesIO actualProperties = testContext.getResponseBody();
            actualProperties.getValuedProperties().forEach(valuedProperty -> {
                if (moduleBuilder.isPasswordProperty(valuedProperty.getName())) {
                    assertEquals("********", valuedProperty.getValue());
                }
            });
        });

        Then("^the non-password property values are not obfuscated$", () -> {
            assertOK();
            PropertiesIO actualProperties = testContext.getResponseBody();
            actualProperties.getValuedProperties().forEach(valuedProperty -> {
                if (!moduleBuilder.isPasswordProperty(valuedProperty.getName())) {
                    assertThat(valuedProperty.getValue()).doesNotContain("********");
                }
            });
        });

        Then("^the password property values are not obfuscated$", () -> {
            assertOK();
            PropertiesIO actualProperties = testContext.getResponseBody();
            actualProperties.getValuedProperties().forEach(valuedProperty -> {
                if (moduleBuilder.isPasswordProperty(valuedProperty.getName())) {
                    assertThat(valuedProperty.getValue()).doesNotContain("********");
                }
            });
        });
    }
}
