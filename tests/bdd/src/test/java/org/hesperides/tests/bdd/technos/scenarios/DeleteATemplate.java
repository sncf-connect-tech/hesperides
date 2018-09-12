package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteATemplate implements En {

    @Autowired
    private TechnoContext technoContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    public DeleteATemplate() {
        When("^deleting the template in this techno$", () -> {
            technoContext.deleteTemplate(TemplateBuilder.DEFAULT_NAME);
        });

        Then("^the template in this techno is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrieveTemplate();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToRetrieveTemplate() {
        return rest.doWithErrorHandlerDisabled(rest -> {
            String templateURI = technoContext.getTemplateURI(TemplateBuilder.DEFAULT_NAME);
            return rest.getForEntity(templateURI, String.class);
        });
    }
}
