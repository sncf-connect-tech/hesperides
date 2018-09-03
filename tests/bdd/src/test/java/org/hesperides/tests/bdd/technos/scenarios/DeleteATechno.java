package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATechno extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    public DeleteATechno() {
        When("^deleting this techno", () -> {
            technoContext.deleteTechno();
        });

        Then("^the techno is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrieveTechno();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToRetrieveTechno() {
        return rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(technoContext.getTemplatesURI(), String.class));
    }
}
