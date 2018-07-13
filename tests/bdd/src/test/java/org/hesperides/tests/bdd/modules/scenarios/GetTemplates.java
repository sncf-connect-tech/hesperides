package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetTemplates extends CucumberSpringBean implements En {

    @Autowired
    private TemplateContext templateContext;

    private ResponseEntity<PartialTemplateIO[]> response;
    private List<TemplateIO> templateInputs = new ArrayList<>();

    public GetTemplates() {

        Given("^multiple templates in this module$", () -> {
            for (int i = 0; i < 6; i++) {
                String templateName = TemplateSamples.DEFAULT_NAME + i;
                ResponseEntity<TemplateIO> response = templateContext.addTemplateToExistingModule(templateName);
                templateInputs.add(response.getBody());
            }
        });

        When("^retrieving those templates$", () -> {
            response = rest.getTestRest().getForEntity(templateContext.getTemplatesURI(), PartialTemplateIO[].class);
        });

        Then("^the templates are retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PartialTemplateIO> templateOutputs = Arrays.asList(response.getBody());
            assertEquals(templateInputs.size(), templateOutputs.size());
            //TODO Vérifier le contenu de chaque template ?
        });
    }
}
