package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

public class GetTechnosVersions extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;

    public GetTechnosVersions() {

        When("^I get the techno versions$", () -> {
            technoClient.getVersions("new-techno");
        });
    }
}
