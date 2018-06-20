package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATemplate extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    public DeleteATemplate() {
        When("^deleting the template in this techno$", () -> {
            technoContext.deleteTemplate(TemplateSamples.DEFAULT_NAME);
        });

        Then("^the template in this techno is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrieveTemplate();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToRetrieveTemplate() {
        return rest.doWithErrorHandlerDisabled(rest -> {
            String templateURI = technoContext.getTemplateURI(TemplateSamples.DEFAULT_NAME);
            return rest.getForEntity(templateURI, String.class);
        });
    }
}
