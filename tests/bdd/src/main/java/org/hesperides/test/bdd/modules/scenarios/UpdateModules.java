package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class UpdateModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private TechnoBuilder technoBuilder;

    public UpdateModules() {

        When("^I( try to)? update this module" +
                "( using the wrong version_id)?" +
                "( adding this techno)?$", (
                String tryTo,
                String usingTheWrongVersionId,
                String addingThisTechno) -> {

            if (isNotEmpty(usingTheWrongVersionId)) {
                moduleBuilder.withVersionId(2049);
            }
            if (isNotEmpty(addingThisTechno)) {
                moduleBuilder.withTechnoBuilder(technoBuilder);
            }

            moduleClient.updateModule(moduleBuilder.build(), tryTo);

            if (isEmpty(tryTo)) {
                moduleHistory.updateModuleBuilder(moduleBuilder);
            }
        });

        Then("^the module is successfully updated$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody(ModuleIO.class);
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module update is rejected with a not found error$", this::assertNotFound);

        Then("^the module update is rejected with a conflict error$", this::assertConflict);

        Then("^the module update is rejected with a bad request error$", this::assertBadRequest);
    }
}
