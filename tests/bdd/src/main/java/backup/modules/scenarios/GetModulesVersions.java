package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;

public class GetModulesVersions extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;

    public GetModulesVersions() {

        When("^I get the module versions$", () -> {
            testContext.setResponseEntity(moduleClient.getVersions("new-module"));
        });
    }
}
