package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModulesVersions extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModulesVersions() {

        Given("^a module with (\\d+) versions$", (Integer nbVersions) -> {
            moduleBuilder.withName("new-module");
            for (int i = 0; i < nbVersions; i++) {
                moduleBuilder.withVersion("1." + i);
                moduleClient.create(moduleBuilder.build());
            }
        });

        When("^I get the module versions$", () -> {
            testContext.responseEntity = moduleClient.getVersions("new-module");
        });

        Then("^a list of (\\d+) versions is returned$", (Integer nbVersions) -> {
            assertOK();
            assertEquals(nbVersions.intValue(), getBodyAsArray().length);
        });
    }
}
