package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATemplate extends CucumberSpringBean implements En {

    @Autowired
    private TemplateContext templateContext;

    public DeleteATemplate() {
        When("^deleting this template$", () -> {
            templateContext.deleteTemplate();
        });

        Then("^the template is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrieveTemplate();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    public ResponseEntity<String> failTryingToRetrieveTemplate() {
        return rest.doWithErrorHandlerDisabled(rest ->
                rest.getForEntity(templateContext.getTemplateURI(TemplateSamples.DEFAULT_NAME), String.class));
    }

    /**
     * TODO Tester la tentative de suppression d'un template qui n'existe pas => 404
     */
}
