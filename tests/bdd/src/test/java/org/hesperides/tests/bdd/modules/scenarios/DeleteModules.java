package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;

public class DeleteModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public DeleteModules() {

        Given("^the module is deleted", () -> {
            moduleClient.delete(moduleBuilder.build());
        });

        When("^I( try to)? delete this module$", (String tryTo) -> {
            testContext.responseEntity = moduleClient.delete(moduleBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
        });

        Then("^the module is successfully deleted$", () -> {
            assertOK();
            testContext.responseEntity = moduleClient.get(moduleBuilder.build(), moduleBuilder.getVersionType(), String.class);
            assertNotFound();
        });

        Then("^the module deletion is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }
}
