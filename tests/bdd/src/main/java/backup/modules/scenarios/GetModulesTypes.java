package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModulesTypes extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public GetModulesTypes() {

        When("^I get the module types$", () -> {
            ModuleIO module = moduleBuilder.build();
            testContext.setResponseEntity(moduleClient.getTypes(module.getName(), module.getVersion()));
        });
    }
}
