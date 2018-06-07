package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.PropertiesModelOutput;
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

    private ResponseEntity<PropertiesModelOutput> response;

    public GetModel() {
        Given("^templates with properties in this techno$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("foo", "foo={{foo}}"));
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("bar", "bar={{bar}}"));
        });

        When("^retrieving the model of this tehno$", () -> {
            response = rest.getTestRest().getForEntity(technoContext.getTemplatesURI() + "/model", PropertiesModelOutput.class);
        });

        Then("^I get the properties model of this techno$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PropertiesModelOutput propertiesModelOutput = response.getBody();
            assertEquals(2, propertiesModelOutput.getKeyValueProperties().size());
            assertEquals("foo", propertiesModelOutput.getKeyValueProperties().get(0).getName());
            assertEquals("bar", propertiesModelOutput.getKeyValueProperties().get(1).getName());
        });
    }
}
