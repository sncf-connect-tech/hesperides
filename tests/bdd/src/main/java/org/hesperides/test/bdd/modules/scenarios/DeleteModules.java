package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

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
            testContext.setResponseEntity(moduleClient.delete(moduleBuilder.build(), getResponseType(tryTo, ResponseEntity.class)));
        });

        Then("^the module is successfully deleted$", () -> {
            assertOK();
            testContext.setResponseEntity(moduleClient.get(moduleBuilder.build(), moduleBuilder.getVersionType(), String.class));
            assertNotFound();
        });

        Then("^the module deletion is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }
}
