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
import org.hesperides.core.presentation.io.platforms.properties.PropertiesOutput;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetProperties implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity<PropertiesOutput> responseEntity;

    public GetProperties() {

        When("^I get this platform properties$", () -> {
            responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
        });

        Then("^the platform properties are successfully retrieved$", () -> {
            assertOK(responseEntity);
            PropertiesOutput expectedProperties = platformBuilder.getPropertiesOutput();
            PropertiesOutput actualProperties = responseEntity.getBody();
            assertEquals(expectedProperties, actualProperties);
        });
    }
}
