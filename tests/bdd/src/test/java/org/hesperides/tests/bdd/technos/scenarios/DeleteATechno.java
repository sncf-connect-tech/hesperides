package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.ExistingTechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATechno extends CucumberSpringBean implements En {

    @Autowired
    private ExistingTechnoContext existingTechno;

    public DeleteATechno() {
        When("^deleting this techno", () -> {
            rest.getTestRest().delete(existingTechno.getTechnoLocation());
        });

        Then("^the techno is successfully deleted$", () -> {
            //TODO Sortir cet appel dans une méthode commune et faire de même pour template et module
            ResponseEntity<String> response = rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(existingTechno.getTechnoLocation() + "/templates", String.class));
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }
}
