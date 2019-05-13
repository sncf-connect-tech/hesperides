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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RestorePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    public RestorePlatforms() {

        When("^I( try to)? restore this platform( with a different platform case)?$", (String tryTo, String withDifferentPlatformCase) -> {
            if (isNotBlank(withDifferentPlatformCase)) {
                platformBuilder.withPlatformName(platformBuilder.getPlatformName().toLowerCase());
            }
            testContext.responseEntity = platformClient.restore(platformBuilder.buildInput(), getResponseType(tryTo, ResponseEntity.class));
            platformBuilder.incrementVersionId();
        });
    }
}
