package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetTechnosName extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;

    public GetTechnosName() {

        When("^I get the technos names$", () -> {
            testContext.responseEntity = technoClient.getNames();
        });
    }
}
