package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class AddATemplate extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private TemplateIO templateInput;
    private ResponseEntity<TemplateIO> response;

    public AddATemplate() {
        When("^adding a template to this techno$", () -> {
            templateInput = TemplateSamples.getTemplateInputWithName("anotherone");
            response = technoContext.addTemplateToExistingTechno(templateInput);
        });

        Then("^the template is successfully added to the techno$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplate(templateInput, templateOutput, technoContext.getNamespace(), 1);
        });
    }
}
