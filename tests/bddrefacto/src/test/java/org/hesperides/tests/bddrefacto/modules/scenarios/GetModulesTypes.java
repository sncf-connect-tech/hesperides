package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetModulesTypes implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<String[]> responseEntity;

    public GetModulesTypes() {

        When("^I get the module types$", () -> {
            ModuleIO module = moduleBuilder.build();
            responseEntity = moduleClient.getTypes(module.getName(), module.getVersion());
        });

        Then("^a list containing workingcopy and release is returned$", () -> {
            assertOK(responseEntity);
            assertEquals(2, responseEntity.getBody().length);
            assertEquals("workingcopy", responseEntity.getBody()[0]);
            assertEquals("release", responseEntity.getBody()[1]);
        });

        Then("^a list containing workingcopy is returned$", () -> {
            assertOK(responseEntity);
            assertEquals(1, responseEntity.getBody().length);
            assertEquals("workingcopy", responseEntity.getBody()[0]);
        });

        Then("^a list containing nothing is returned$", () -> {
            assertOK(responseEntity);
            assertEquals(0, responseEntity.getBody().length);
        });
    }
}
