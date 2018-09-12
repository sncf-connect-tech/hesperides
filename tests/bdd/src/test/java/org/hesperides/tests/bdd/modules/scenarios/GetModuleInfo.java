package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.ModuleAssertions;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetModuleInfo implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<ModuleIO> response;

    public GetModuleInfo() {

        When("^retrieving the module's info$", () -> {
            response = retrieveExistingModule();
        });

        Then("^the module's info is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            ModuleAssertions.assertModuleAgainstDefaultValues(moduleOutput, 1);
        });
    }

    private ResponseEntity<ModuleIO> retrieveExistingModule() {
        return rest.getTestRest().getForEntity(moduleContext.getModuleURI(), ModuleIO.class);
    }

    // TODO Tester un module avec des technos
}
