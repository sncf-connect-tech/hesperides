package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteAModule implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    public DeleteAModule() {
        When("^deleting this module$", () -> {
            rest.getTestRest().delete(moduleContext.getModuleURI());
        });

        Then("^the module is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrieveModule();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToRetrieveModule() {
        return rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(moduleContext.getModuleURI(), String.class));
    }

    /**
     * TODO Tester la tentative de suppression d'un module qui n'existe pas => 404
     */
}
