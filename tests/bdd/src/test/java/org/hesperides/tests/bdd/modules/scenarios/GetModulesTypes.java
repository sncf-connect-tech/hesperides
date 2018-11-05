package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModulesTypes extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModulesTypes() {

        When("^I get the module types$", () -> {
            ModuleIO module = moduleBuilder.build();
            responseEntity = moduleClient.getTypes(module.getName(), module.getVersion());
        });

        Then("^a list containing workingcopy and release is returned$", () -> {
            assertOK();
            String[] body = getBodyAsArray();
            assertEquals(2, body.length);
            assertEquals("workingcopy", body[0]);
            assertEquals("release", body[1]);
        });

        Then("^a list containing workingcopy is returned$", () -> {
            assertOK();
            String[] body = getBodyAsArray();
            assertEquals(1, body.length);
            assertEquals("workingcopy", body[0]);
        });
    }
}
