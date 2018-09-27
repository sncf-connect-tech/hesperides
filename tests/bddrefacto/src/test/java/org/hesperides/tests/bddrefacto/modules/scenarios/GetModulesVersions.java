package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetModulesVersions implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<String[]> responseEntity;

    public GetModulesVersions() {

        Given("^a module with (\\d+) versions$", (final Integer nbVersions) -> {
            moduleBuilder.withName("new-module");
            for (int i = 0; i < nbVersions; i++) {
                moduleBuilder.withVersion("1." + i);
                moduleClient.create(moduleBuilder.build());
            }
        });

        When("^I get the module versions$", () -> {
            responseEntity = moduleClient.getVersions("new-module");
        });

        Then("^a list of (\\d+) versions is returned$", (final Integer nbVersions) -> {
            assertOK(responseEntity);
            assertEquals(nbVersions.intValue(), responseEntity.getBody().length);
        });
    }
}
