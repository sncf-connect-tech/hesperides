package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.TechnosSamples;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATechno extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private TechnoIO technoInput;
    private TemplateIO templateInput;
    private ResponseEntity<TemplateIO> response;

    public CreateATechno() {
        Given("^a techno to create$", () -> {
            templateInput = TemplateSamples.getTemplateInputWithDefaultValues();
            technoInput = TechnosSamples.getTechnoWithDefaultValues();
        });

        When("^creating a new techno$", () -> {
            response = technoContext.createTechno(technoInput, templateInput);
        });

        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, technoContext.getNamespace(), 1);
        });
    }
}
