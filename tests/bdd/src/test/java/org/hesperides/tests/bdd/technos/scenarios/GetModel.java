package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.ModelOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainer.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetModel extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity<ModelOutput> response;

    public GetModel() {
        Given("^templates with properties in this techno$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("foo", "foo={{foo}}"));
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("bar", "bar={{bar}}"));
        });

        When("^retrieving the model of this tehno$", () -> {
            response = rest.getTestRest().getForEntity(technoContext.getTechnoURI() + "/model", ModelOutput.class);
        });

        Then("^I get the properties model of this techno$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            assertEquals("foo", modelOutput.getProperties().get(0).getName());
            assertEquals("bar", modelOutput.getProperties().get(1).getName());
        });
    }

    /**
     * TODO Tester :
     * - Les iterables
     * - Les propriétés qui ont le même nom dans des templates différents avec des options différentes
     * - Les propriétés dans filename et location
     * - Les propriétés qui ont le même nom avec des options différentes dans filename, location et content
     */
}