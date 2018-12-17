package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class UpdateModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public UpdateModules() {

        Given("^the module is outdated$", () -> {
            moduleBuilder.withVersionId(0);
        });

        Given("^this techno is associated to this module$", () -> {
            moduleBuilder.withTechno(technoBuilder.build());
        });

        When("^I( try to)? update this module$", (String tryTo) -> {
            testContext.responseEntity = moduleClient.update(moduleBuilder.build(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module is successfully updated$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionId(2).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module update is rejected with a not found error$", () -> {
            assertNotFound();
        });

        Then("^the module update is rejected with a conflict error$", () -> {
            assertConflict();
        });

        Then("^the module update is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }
}
