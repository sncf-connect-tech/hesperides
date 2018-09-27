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
import org.hesperides.tests.bddrefacto.platforms.PlatformBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;

public class DeletePlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity responseEntity;

    public DeletePlatforms() {

        Given("^a platform that doesn't exist$", () -> {
            platformBuilder.withPlatformName("nope");
        });

        When("^I( try to)? delete this platform$", (final String tryTo) -> {
            responseEntity = platformClient.delete(platformBuilder.buildInput(), getResponseType(tryTo, ResponseEntity.class));
        });

        Then("^the platform is successfully deleted", () -> {
            assertOK(responseEntity);
        });

        Then("^the platform deletion is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
