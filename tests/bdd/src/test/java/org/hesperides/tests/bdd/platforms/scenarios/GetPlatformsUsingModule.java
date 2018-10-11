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
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.hesperides.tests.bdd.commons.StepHelper.assertOK;

public class GetPlatformsUsingModule implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<ModulePlatformsOutput[]> responseEntity;

    public GetPlatformsUsingModule() {

        When("^I get the platforms using this module$", () -> {
            responseEntity = platformClient.getPlatformsUsingModule(moduleBuilder.build());
        });

        Then("^the platforms using this module are successfully retrieved", () -> {
            assertOK(responseEntity);
            List<ModulePlatformsOutput> expectedPlatforms = platformBuilder.buildModulePlatforms();
            List<ModulePlatformsOutput> actualPlatforms = Arrays.asList(responseEntity.getBody());
            Assert.assertEquals(expectedPlatforms, actualPlatforms);
        });
    }
}
