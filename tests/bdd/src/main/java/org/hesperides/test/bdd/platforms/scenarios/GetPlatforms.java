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
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public GetPlatforms() {

        When("^(?:when )?I( try to)? get the platform detail( at a specific time in the past)?( at the time of the EPOCH)?( with the wrong letter case)?$", (String tryTo, String withTimestamp, String withEpochTimestamp, String withWrongLetterCase) -> {
            Long timestamp = null;
            if (StringUtils.isNotEmpty(withTimestamp)) {
                timestamp = platformHistory.getFirstPlatformTimestamp();
            } else if (StringUtils.isNotEmpty(withEpochTimestamp)) {
                timestamp = 0L;
            }
            PlatformIO platformInput = platformBuilder.buildInput();
            if (StringUtils.isNotEmpty(withWrongLetterCase)) {
                platformInput = new PlatformBuilder().withPlatformName(platformBuilder.getPlatformName().toUpperCase()).buildInput();
            }
            testContext.setResponseEntity(platformClient.get(platformInput, timestamp, getResponseType(tryTo, PlatformIO.class)));
        });

        Then("^the( initial)? platform detail is successfully retrieved", (String initial) -> {
            assertOK();
            PlatformIO expectedPlatform = StringUtils.isNotEmpty(initial) ? platformHistory.getInitialPlatformState() : platformBuilder.buildOutput();
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^there is (\\d+) module on this(?: new)? platform$", (Integer moduleCount) -> {
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            assertThat(actualPlatform.getDeployedModules(), hasSize(moduleCount));
        });

        Then("^there are (\\d+) instances$", (Integer expectedCount) -> {
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            int instancesCount = actualPlatform.getDeployedModules().stream()
                    .mapToInt(deployedModule -> deployedModule.getInstances().size())
                    .sum();
            assertEquals(expectedCount.intValue(), instancesCount);
        });
    }
}
