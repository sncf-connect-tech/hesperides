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
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.hesperides.test.bdd.platforms.OldPlatformHistory;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPlatforms extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldPlatformHistory oldPlatformHistory;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public GetPlatforms() {

        When("^(?:when )?I( try to)? get the platform detail" +
                "( at a specific time in the past)?" +
                "( at the time of the EPOCH)?" +
                "( with the wrong letter case)?" +
                "( requesting the password flag)?$", (
                String tryTo,
                String withTimestamp,
                String withEpochTimestamp,
                String withWrongLetterCase,
                String requestingThePasswordFlag) -> {
            Long timestamp = null;
            if (StringUtils.isNotEmpty(withTimestamp)) {
                timestamp = oldPlatformHistory.getFirstPlatformTimestamp();
            } else if (StringUtils.isNotEmpty(withEpochTimestamp)) {
                timestamp = 0L;
            }
            PlatformIO platformInput = oldPlatformBuilder.buildInput();
            if (StringUtils.isNotEmpty(withWrongLetterCase)) {
                platformInput = new OldPlatformBuilder().withPlatformName(oldPlatformBuilder.getPlatformName().toUpperCase()).buildInput();
            }
            testContext.setResponseEntity(oldPlatformClient.get(platformInput, timestamp, StringUtils.isNotEmpty(requestingThePasswordFlag), getResponseType(tryTo, PlatformIO.class)));
        });

        Then("^the( initial)? platform detail is successfully retrieved", (String initial) -> {
            assertOK();
            PlatformIO expectedPlatform;
            if (StringUtils.isEmpty(initial)) {
                expectedPlatform = oldPlatformBuilder.buildOutput();
            } else {
                expectedPlatform = oldPlatformHistory.getInitialPlatformState();
                expectedPlatform = new PlatformIO(
                        expectedPlatform.getPlatformName(),
                        expectedPlatform.getApplicationName(),
                        expectedPlatform.getVersion(),
                        expectedPlatform.getIsProductionPlatform(),
                        expectedPlatform.getDeployedModules().stream().map(deployedModule -> new DeployedModuleIO(
                                deployedModule.getId(),
                                moduleBuilder.getPropertiesVersionId(), // Tout ça pour ça, ne fonctionne pas avec plusieurs modules... REFACTORING NECESSAIRE
                                deployedModule.getName(),
                                deployedModule.getVersion(),
                                deployedModule.getIsWorkingCopy(),
                                deployedModule.getModulePath(),
                                deployedModule.getPropertiesPath(),
                                deployedModule.getInstances()))
                                .collect(Collectors.toList()),
                        expectedPlatform.getVersionId(),
                        null);
            }
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^there is (\\d+) module on this(?: new)? platform$", (Integer moduleCount) -> {
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            assertThat(actualPlatform.getDeployedModules(), hasSize(moduleCount));
        });

        Then("^there are (\\d+) instances$", (Integer expectedCount) -> {
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            int instancesCount = actualPlatform.getDeployedModules().stream()
                    .mapToInt(deployedModule -> deployedModule.getInstances().size())
                    .sum();
            assertEquals(expectedCount.intValue(), instancesCount);
        });

        Then("^the platform has the password flag and the flag is set to (true|false)?$", (String trueOrFalse) -> {
            Boolean hasPasswords = testContext.getResponseBody(PlatformIO.class).getHasPasswords();
            Assertions.assertThat(hasPasswords).isNotNull();
            assertEquals("true".equals(trueOrFalse), hasPasswords);
        });
    }
}
