package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.templatecontainer.contexts.TemplateSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATechno extends CucumberSpringBean implements En {

    @Autowired
    private TemplateSample templateSample;

    private String technoName;
    private String technoVersion;
    private TemplateIO templateInput;
    private ResponseEntity<TemplateIO> response;

    public CreateATechno() {
        // TODO Factoriser ce code et celui défini dans ExistingTechnoContext
        Given("^a techno to create$", () -> {
            technoName = "technoName";
            technoVersion = "technoVersion";
            templateInput = templateSample.getTemplateInput();
        });

        When("^creating a new techno$", () -> {
            response = rest.getTestRest().postForEntity(
                    "/templates/packages/{technoName}/{technoVersion}/workingcopy/templates",
                    templateInput,
                    TemplateIO.class,
                    technoName,
                    technoVersion);
        });


        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            templateSample.assertTemplateProperties(templateOutput, "technos#" + technoName + "#" + technoVersion + "#WORKINGCOPY", 1);
        });
    }
}
