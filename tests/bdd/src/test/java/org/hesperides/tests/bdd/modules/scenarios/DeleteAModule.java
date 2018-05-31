package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteAModule extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext existingModule;

    public DeleteAModule() {
        When("^deleting this module$", () -> {
            rest.getTestRest().delete(existingModule.getModuleURI());
        });

        Then("^the module is successfully deleted$", () -> {
            ResponseEntity<String> response = rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(existingModule.getModuleURI(), String.class));
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    /**
     * TODO Tester la tentative de suppression d'un module qui n'existe pas => 404
     */
}
