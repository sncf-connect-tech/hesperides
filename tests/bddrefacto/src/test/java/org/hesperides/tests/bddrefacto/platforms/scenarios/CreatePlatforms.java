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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.containsString;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.getResponseType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreatePlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity responseEntity;

    public CreatePlatforms() {

        Given("^an existing platform( using this module)?$", (final String usingThisModule) -> {
            if (StringUtils.isNotEmpty(usingThisModule)) {
                platformBuilder.withModule(moduleBuilder.build());
            }
            platformClient.create(platformBuilder.buildInput());
            platformBuilder.withVersionId(1);
        });

        Given("^a platform to create(?:, named \"([^\"]*)\")?$", (final String name) -> {
            if (StringUtils.isNotEmpty(name)) {
                platformBuilder.withPlatformName(name);
            }
        });

        When("^I( try to)? create this platform$", (final String tryTo) -> {
            responseEntity = platformClient.create(platformBuilder.buildInput(), getResponseType(tryTo, PlatformOutput.class));
        });

        Then("^the platform is successfully created$", () -> {
            assertOK(responseEntity);
            PlatformOutput expectedPlatform = platformBuilder.buildOutput();
            PlatformOutput actualPlatform = (PlatformOutput) responseEntity.getBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), responseEntity.getStatusCode());
            assertThat((String) responseEntity.getBody(), containsString(message));
        });
    }
}
