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
package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hesperides.tests.bdd.commons.StepHelper.assertOK;

public class GetGlobalPropertiesUsage implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<Map<String, Set<GlobalPropertyUsageOutput>>> responseEntity;

    public GetGlobalPropertiesUsage() {

        Given("^the deployed module properties are valued with the platform global properties$", () -> {
            platformBuilder.withProperty("module-foo", "{{global-module-foo}}");
            platformBuilder.withProperty("techno-foo", "{{global-techno-foo}}");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(), moduleBuilder.getPropertiesPath());
            platformBuilder.withGlobalProperty("global-module-foo", "whatever", true, true);
            platformBuilder.withGlobalProperty("global-techno-foo", "whatever", true, true);
        });

        When("^I get this platform global properties usage$", () -> {
            responseEntity = platformClient.getGlobalPropertiesUsage(platformBuilder.buildInput());
        });

        Then("^the platform global properties usage is successfully retrieved$", () -> {
            assertOK(responseEntity);

            Map<String, Set<GlobalPropertyUsageOutput>> expectedProperties = new HashMap<>();
            platformBuilder.getProperties().forEach(property -> {
                if (property.isGlobal()) {
                    Set<GlobalPropertyUsageOutput> usages = new HashSet<>();
                    if (property.isUsed()) {
                        usages.add(new GlobalPropertyUsageOutput(property.isInModel(), moduleBuilder.getPropertiesPath()));
                    }
                    expectedProperties.put(property.getName(), usages);
                }
            });

            Map<String, Set<GlobalPropertyUsageOutput>> actualProperties = responseEntity.getBody();
            Assert.assertEquals(expectedProperties, actualProperties);
        });
    }
}
