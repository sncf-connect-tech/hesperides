package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

public class GetTechnosName extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;

    public GetTechnosName() {

        When("^I get the technos names$", () -> {
            testContext.setResponseEntity(technoClient.getNames());
        });
    }
}
