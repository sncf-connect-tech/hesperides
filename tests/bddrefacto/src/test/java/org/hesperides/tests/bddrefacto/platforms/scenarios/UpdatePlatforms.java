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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.*;

public class UpdatePlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity responseEntity;

    public UpdatePlatforms() {

        When("^updating this platform(, requiring properties copy)?$", (String withCopy) -> {
            responseEntity = platformClient.update(platformBuilder.buildInput(), withCopy != null);
        });

        Then("^the platform is successfully updated(?:, but system warns about \"([^\"]+)\")?", (String warning) -> {
            assertOK(responseEntity);
            if (warning != null) {
                final List<String> warnings = responseEntity.getHeaders().get("x-hesperides-warning");
                assertTrue("expected at least 1 custom warning", warnings != null && warnings.size() > 0);
                assertThat(warnings, hasItem(containsString(warning)));
            }
            PlatformOutput expectedPlatformOutput = platformBuilder.withVersionId(2).buildOutput();
            PlatformOutput actualPlatform = (PlatformOutput) responseEntity.getBody();
            assertEquals(expectedPlatformOutput, actualPlatform);
        });
    }
}
