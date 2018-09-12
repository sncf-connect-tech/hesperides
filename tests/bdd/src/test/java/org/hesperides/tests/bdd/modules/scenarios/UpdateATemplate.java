package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class UpdateATemplate implements En {

    @Autowired
    private TemplateContext templateContext;
    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity response;

    public UpdateATemplate() {

        When("^updating this template$", () -> {
            TemplateIO templateInput = new TemplateBuilder().withVersionId(1).build();
            response = templateContext.updateTemplate(templateInput);
        });

        When("^updating the same template at the same time$", () -> {
            TemplateIO templateInput = new TemplateBuilder().withVersionId(1).build();
            response = failTryingToUpdateTemplate(templateInput);
        });

        Then("^the template is successfully updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, moduleContext.getNamespace(), 2L);
        });

        Then("^the template update is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private ResponseEntity failTryingToUpdateTemplate(TemplateIO templateInput) {
        return rest.doWithErrorHandlerDisabled(rest ->
                rest.exchange(templateContext.getTemplatesURI(), HttpMethod.PUT, new HttpEntity<>(templateInput), String.class));
    }

    // TODO Tester la tentative de modification d'un template qui n'existe pas => 404
}
