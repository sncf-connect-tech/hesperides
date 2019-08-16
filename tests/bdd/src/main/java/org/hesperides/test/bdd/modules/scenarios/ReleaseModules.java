package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

public class ReleaseModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public ReleaseModules() {

        When("^I( try to)? release this module$", (String tryTo) -> {
            moduleClient.releaseModule(moduleBuilder.build(), tryTo);
            moduleBuilder.withVersionType(VersionType.RELEASE);
            moduleBuilder.updateTemplatesNamespace();
            moduleHistory.addModuleBuilder(moduleBuilder);
        });

        Then("^the module release is rejected with a not found error$", this::assertNotFound);

        Then("^the module release is rejected with a conflict error$", this::assertConflict);
    }
}
