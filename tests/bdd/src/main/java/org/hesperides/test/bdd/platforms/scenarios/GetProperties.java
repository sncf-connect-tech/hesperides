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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetProperties() {

        When("^I get the platform properties for this module$", () -> {
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
        });

        When("^I get the global properties of this platform$", () -> {
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), "#");
        });

        Then("^the platform( global)? properties are successfully retrieved$", (String global) -> {
            assertOK();
            PropertiesIO expectedProperties = platformBuilder.getProperties(StringUtils.isNotEmpty(global));
            PropertiesIO actualProperties = ((ResponseEntity<PropertiesIO>)testContext.responseEntity).getBody();
            assertEquals(expectedProperties, actualProperties);
        });
    }
}
