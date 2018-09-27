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
package org.hesperides.tests.bddrefacto.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;
import org.hesperides.tests.bddrefacto.platforms.PlatformBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;

public class GetPlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity responseEntity;

    public GetPlatforms() {

        When("^I( try to)? get the platform detail$", (final String tryTo) -> {
            responseEntity = platformClient.get(platformBuilder.buildInput(), getResponseType(tryTo, PlatformOutput.class));
        });

        Then("^the platform detail is successfully retrieved", () -> {
            assertOK(responseEntity);
            PlatformOutput expectedPlatform = platformBuilder.buildOutput();
            PlatformOutput actualPlatform = (PlatformOutput) responseEntity.getBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform is not found$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
