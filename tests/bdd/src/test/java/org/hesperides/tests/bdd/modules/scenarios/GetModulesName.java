package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModulesName extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;

    public GetModulesName() {

        When("^I get the modules name$", () -> {
            responseEntity = moduleClient.getNames();
        });

        Then("^a list of (\\d+) names? is returned$", (Integer nbModulesName) -> {
            assertOK();
            assertEquals(nbModulesName.intValue(), getBodyAsArray().length);
        });
    }
}
