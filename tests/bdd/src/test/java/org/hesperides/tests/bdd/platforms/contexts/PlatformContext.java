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
package org.hesperides.tests.bdd.platforms.contexts;

import cucumber.api.java8.En;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.presentation.io.platforms.PlatformIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.http.ResponseEntity;

public class PlatformContext extends CucumberSpringBean implements En {

    private Platform.Key platformKey;

    public PlatformContext() {
        Given("^an existing platform", () -> {
            createPlatform();
        });
    }


    public Platform.Key getPlatformKey() {
        return platformKey;
    }

    private void createPlatform() {
        PlatformIO platformInput = PlatformSamples.getPlatformInputWithDefaultValues();
        createPlatform(platformInput);
    }

    public ResponseEntity<PlatformIO> createPlatform(PlatformIO platformInput) {
        ResponseEntity<PlatformIO> response = rest.getTestRest().postForEntity(
                "/applications/{{application_name}}/platforms", platformInput, PlatformIO.class, platformInput.getApplicationName(), platformInput.getPlatformName());
        PlatformIO platformOutput = response.getBody();
        platformKey = new Platform.Key(platformOutput.getApplicationName(), platformOutput.getPlatformName(), platformOutput.getVersion());
        return response;
    }
}
