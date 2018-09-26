package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetModulesName implements En {

    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity<String[]> responseEntity;

    public GetModulesName() {

        When("^I get the modules name$", () -> {
            responseEntity = moduleClient.getNames();
        });

        Then("^a list of (\\d+) names? is returned$", (final Integer nbModulesName) -> {
            assertOK(responseEntity);
            assertEquals(nbModulesName.intValue(), responseEntity.getBody().length);
        });
    }
}
