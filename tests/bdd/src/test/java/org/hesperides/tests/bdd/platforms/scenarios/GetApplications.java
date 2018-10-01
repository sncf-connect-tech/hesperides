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
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.*;

public class GetApplications implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity responseEntity;
    private boolean hidePlatform;

    public GetApplications() {

        When("^I( try to)? get the platform application( with parameter hide_platform set to true)?$", (final String tryTo, final String withHidePlatform) -> {
            hidePlatform = withHidePlatform != null;
            responseEntity = platformClient.getApplication(
                    platformBuilder.build(),
                    hidePlatform,
                    getResponseType(tryTo, ApplicationOutput.class));
        });

        Then("^the application is successfully retrieved", () -> {
            assertOK(responseEntity);
            ApplicationOutput expectedApplication = platformBuilder.buildApplicationOutput(hidePlatform);
            ApplicationOutput actualApplication = (ApplicationOutput) responseEntity.getBody();
            Assert.assertEquals(expectedApplication, actualApplication);
        });

        Then("^the application is not found$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
