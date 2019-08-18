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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class DeletePlatforms extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;

    public DeletePlatforms() {

        Given("^a platform that doesn't exist$", () -> {
            oldPlatformBuilder.withPlatformName("nope");
        });

        When("^I( try to)? delete this platform$", (String tryTo) -> {
            testContext.setResponseEntity(oldPlatformClient.delete(oldPlatformBuilder.buildInput(), getResponseType(tryTo, ResponseEntity.class)));
        });

        Then("^the platform is successfully deleted", () -> {
            assertOK();
        });

        Then("^the platform deletion is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }
}
