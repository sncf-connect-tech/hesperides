package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

public class GetModulesVersions extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;

    public GetModulesVersions() {

        When("^I get the module versions$", () -> {
            testContext.setResponseEntity(moduleClient.getVersions("new-module"));
        });
    }
}
