package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

public class GetTechnosVersions extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnosVersions() {

        When("^I get the techno versions$", () -> {
            testContext.setResponseEntity(technoClient.getVersions("new-techno"));
        });
    }
}
