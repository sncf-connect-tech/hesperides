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
package org.hesperides.test.bdd.keyvalues.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.keyvalues.KeyValueOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.keyvalues.KeyValueBuilder;
import org.hesperides.test.bdd.keyvalues.KeyValueClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class UpdateKeyValues extends HesperidesScenario implements En {

    @Autowired
    private KeyValueBuilder keyValueBuilder;
    @Autowired
    private KeyValueClient keyValueClient;

    public UpdateKeyValues() {

        When("^I update this key value$", () -> {
            keyValueBuilder.withKey("foo");
            keyValueBuilder.withValue("bar");
            keyValueClient.updateKeyValue(keyValueBuilder.getId(), keyValueBuilder.buildInput());
        });

        Then("^the key value is successfully updated$", () -> {
            assertNoContent();
            KeyValueOutput expectedKeyValue = keyValueBuilder.buildOutput();
            keyValueClient.getKeyValue(keyValueBuilder.getId());
            KeyValueOutput actualKeyValue = testContext.getResponseBody();
            assertEquals(expectedKeyValue, actualKeyValue);
        });
    }
}
