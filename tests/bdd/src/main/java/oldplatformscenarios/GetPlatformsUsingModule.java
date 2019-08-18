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
package oldplatformscenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.hesperides.test.bdd.platforms.OldPlatformHistory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPlatformsUsingModule extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformHistory oldPlatformHistory;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public GetPlatformsUsingModule() {

        When("^I get the platforms using this module$", () -> {
            testContext.setResponseEntity(oldPlatformClient.getPlatformsUsingModule(moduleBuilder.build()));
        });

        Then("^the platforms using this module are successfully retrieved", () -> {
            assertOK();
            List<ModulePlatformsOutput> expectedPlatforms = oldPlatformHistory.buildModulePlatforms();
            List<ModulePlatformsOutput> actualPlatforms = testContext.getResponseBodyAsList();
            assertEquals(expectedPlatforms, actualPlatforms);
        });

        Then("^a single platform is retrieved", () -> {
            assertOK();
            List<ModulePlatformsOutput> actualPlatforms = testContext.getResponseBodyAsList();
            assertThat(actualPlatforms, hasSize(1));
        });
    }
}
