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
package org.hesperides.test.bdd.applications.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.applications.ApplicationBuilder;
import org.hesperides.test.bdd.applications.ApplicationClient;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.fail;

public class UpdateApplications extends HesperidesScenario implements En {

    @Autowired
    private ApplicationClient appClient;
    @Autowired
    private ApplicationBuilder appBuilder;

    public UpdateApplications() {

        When("^I add the authority (.+) to the application", (String groupCN) -> {
            fail("TODO");
        });

        When("^I remove all authorities on the application", () -> {
            fail("TODO");
        });
    }
}
