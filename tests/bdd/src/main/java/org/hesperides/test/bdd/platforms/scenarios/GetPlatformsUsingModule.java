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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPlatformsUsingModule extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetPlatformsUsingModule() {

        When("^I get the platforms using this module$", () -> {
            testContext.setResponseEntity(platformClient.getPlatformsUsingModule(moduleBuilder.build()));
        });

        Then("^the platforms using this module are successfully retrieved", () -> {
            assertOK();
            List<ModulePlatformsOutput> expectedPlatforms = platformHistory.buildModulePlatforms();
            List<ModulePlatformsOutput> actualPlatforms = Arrays.asList(getBodyAsArray());
            assertEquals(expectedPlatforms, actualPlatforms);
        });

        Then("^a single platform is retrieved", () -> {
            assertOK();
            List<ModulePlatformsOutput> actualPlatforms = Arrays.asList(getBodyAsArray());
            assertThat(actualPlatforms, hasSize(1));
        });
    }
}
