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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class UpdatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public UpdatePlatforms() {

        When("^I( try to)? update this platform$", (String tryTo) -> {
            platformBuilder.withVersion("1.1");
            platformClient.updatePlatform(platformBuilder.buildInput(), tryTo);
            if (StringUtils.isEmpty(tryTo)) {
                platformHistory.updatePlatformBuilder(platformBuilder);

            }
        });

        Then("^the platform is successfully updated$", () -> {
            assertOK();
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody();
            assertEquals(expectedPlatform, actualPlatform);
        });

//        Then("^the platform is successfully deleted", this::assertOK);
    }
}
