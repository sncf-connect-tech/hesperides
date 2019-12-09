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

public class CreateKeyValues extends HesperidesScenario implements En {

    @Autowired
    private KeyValueBuilder keyValueBuilder;
    @Autowired
    private KeyValueClient keyValueClient;

    public CreateKeyValues() {

        Given("^a key value to create$", () -> {
            keyValueBuilder.reset();
        });

        Given("^an existing key value$", () -> {
            keyValueBuilder.reset();
            keyValueClient.createKeyValue(keyValueBuilder.buildInput());
            keyValueClient.getKeyValue(testContext.getLocation());
            KeyValueOutput keyValueOutput = testContext.getResponseBody();
            keyValueBuilder.withId(keyValueOutput.getId());
        });

        When("^creating this key value$", () -> {
            keyValueClient.createKeyValue(keyValueBuilder.buildInput());
        });

        Then("^the key value is successfully created$", this::assertCreated);
    }
}
