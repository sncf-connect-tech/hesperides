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
package backup.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleClient;
import org.hesperides.test.bdd.modules.OldModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GetModulesUsingTechno extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private OldModuleHistory moduleHistory;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetModulesUsingTechno() {

        When("^I get the modules using this techno$", () -> {
            testContext.setResponseEntity(moduleClient.getModulesUsingTechno(technoBuilder.build()));
        });

        Then("^the modules using this techno are successfully retrieved", () -> {
            assertOK();
            List<ModuleKeyOutput> expectedModules = moduleHistory.buildTechnoModules();
            List<ModuleKeyOutput> actualModules = testContext.getResponseBodyAsList();
            Assert.assertEquals(expectedModules, actualModules);
        });
    }
}
