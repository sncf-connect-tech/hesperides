package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;

public class GetModulesName extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;

    public GetModulesName() {

        When("^I get the modules name$", () -> {
            testContext.setResponseEntity(moduleClient.getNames());
        });
    }
}
