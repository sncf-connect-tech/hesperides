package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATemplate extends CucumberSpringBean implements En {

    @Autowired
    private ExistingTemplateContext existing;

    public DeleteATemplate() {
        When("^deleting this template$", () -> {
            rest.getTestRest().delete(existing.getTemplateLocation());
        });

        Then("^the template is successfully deleted$", () -> {
            ResponseEntity<String> response = rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(existing.getTemplateLocation(), String.class));
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    /**
     * TODO Tester la tentative de suppression d'un template qui n'existe pas => 404
     */
}
