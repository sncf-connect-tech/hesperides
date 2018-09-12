package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATemplate implements En {

    @Autowired
    private TemplateContext templateContext;
    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private HesperidesTestRestTemplate rest;


    private ResponseEntity response;

    public CreateATemplate() {

        When("^adding a new template to this module$", () -> {
            response = templateContext.addTemplateToExistingModule();
        });

        When("^trying to add the same template to this module$", () -> {
            response = failTryingToAddTemplateToExistingModule();
        });

        Then("^the template is successfully created and the module contains the new template$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, moduleContext.getNamespace(), 1);
        });

        Then("^the second attempt to add the template to the module is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    public ResponseEntity failTryingToAddTemplateToExistingModule() {
        TemplateIO templateInput = new TemplateBuilder().build();
        return rest.doWithErrorHandlerDisabled(rest -> rest.postForEntity(templateContext.getTemplatesURI(), templateInput, String.class));
    }
}
