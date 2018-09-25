package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertNotFound;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;

public class DeleteModules implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity responseEntity;

    public DeleteModules() {

        When("^I( try to)? delete this module$", (final String tryTo) -> {
            responseEntity = moduleClient.delete(moduleBuilder.build(), StepHelper.getResponseType(tryTo, ResponseEntity.class));
        });
        ;

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
