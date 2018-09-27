package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;

public class DeleteModules implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity responseEntity;

    public DeleteModules() {

        Given("^the module is deleted", () -> {
            moduleClient.delete(moduleBuilder.build());
        });

        When("^I( try to)? delete this module$", (final String tryTo) -> {
            responseEntity = moduleClient.delete(moduleBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
        });

        Then("^the module is successfully deleted$", () -> {
            assertOK(responseEntity);
            responseEntity = moduleClient.get(moduleBuilder.build(), String.class);
            assertNotFound(responseEntity);
        });

        Then("^the module deletion is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
