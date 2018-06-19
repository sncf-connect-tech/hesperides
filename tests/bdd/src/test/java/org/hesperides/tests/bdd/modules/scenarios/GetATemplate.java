package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetATemplate extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private TemplateContext templateContext;

    private ResponseEntity<TemplateIO> response;

    public GetATemplate() {

        When("^retrieving this module template$", () -> {
            response = templateContext.retrieveExistingTemplate();
        });

        Then("^the module template is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, moduleContext.getNamespace(), 1);
        });
        And("^an existing template with a name ending in dot sh in this module$", () -> {
            templateContext.addTemplateToExistingModule("template.sh");
        });
        When("^retrieving this module template with a name ending in dot sh$", () -> {
            response = templateContext.retrieveExistingTemplate("template.sh");
        });
        Then("^the module template with a name ending in dot sh is retrieved$", () -> {
            TemplateIO expectedTemplate = TemplateSamples.getTemplateInputWithName("template.sh");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplate(expectedTemplate, templateOutput, moduleContext.getNamespace(), 1);
        });
    }

    /**
     * TODO Tester la tentative de récupération d'un template qui n'existe pas
     */
}
