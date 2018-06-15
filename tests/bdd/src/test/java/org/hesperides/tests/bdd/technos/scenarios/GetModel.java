package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.ModelOutput;
import org.hesperides.presentation.io.PropertyOutput;
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
        Given("^a template that has properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameFilenameLocationAndContent(
                    "template-a",
                    "{{ foo | @comment filename of template-a}}.json",
                    "/{{foo|@comment \"location of template-a\"}}",
                    "{{foo|@required|@comment content of template-a|@default 12|@pattern *|@password }}"));
        });

        When("^retrieving the model of this techno$", () -> {
            response = rest.getTestRest().getForEntity(technoContext.getTechnoURI() + "/model", ModelOutput.class);
        });

        Then("^the model of this techno contains all the properties with the same name from this template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(3, modelOutput.getProperties().size());
            assertProperty(new PropertyOutput("foo", false, "filename of template-a", "", "", false),
                    modelOutput.getProperties().get(0));
            assertProperty(new PropertyOutput("foo", false, "location of template-a", "", "", false),
                    modelOutput.getProperties().get(1));
            assertProperty(new PropertyOutput("foo", true, "content of template-a", "12", "*", true),
                    modelOutput.getProperties().get(2));
        });

        Given("^templates that have properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@default 12|@pattern *|@password }}"));
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-b",
                    "{{foo|@comment \"content of template-b\" }}"));
        });

        Then("^the model of this techno contains all the properties with the same name from these templates$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", true, "content of template-a", "12", "*", true)));
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content of template-b", "", "", false)));
        });

        Given("^a template containing properties that have been updated$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@default 12|@pattern *|@password }}"));
            technoContext.updateTemplate(TemplateSamples.getTemplateInputWithNameContentContentAndVersionId("template-a", "{{ foo }}", 1));
        });

        Then("^the model of this techno contains the updated properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            assertProperty(new PropertyOutput("foo", false, "", "", "", false),
                    modelOutput.getProperties().get(0));
        });

        Given("^a template containing properties but is being deleted$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@default 12|@pattern *|@password }}"));
            technoContext.deleteTemplate("template-a");
        });

        Then("^the model of this techno does not contain the properties of the deleted template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(0, modelOutput.getProperties().size());
        });

        Given("^a template in this techno that has iterable properties$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a", "{{#it1}}{{foo}}{{#it2}}{{bar}}{{/it2}}{{/it1}}"));
        });

        Then("^the model of this techno contains all the iterable properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals("it1", modelOutput.getIterableProperties().get(0).getName());
            assertEquals("foo", modelOutput.getIterableProperties().get(0).getProperties().get(0).getName());
//            assertEquals("it2", modelOutput.getIterableProperties().get(0).getItProperties().get(0).getName());
        });
    }

    private void assertProperty(PropertyOutput expectedProperty, PropertyOutput actualProperty) {
        assertEquals(expectedProperty.getName(), actualProperty.getName());
        assertEquals(expectedProperty.isRequired(), actualProperty.isRequired());
        assertEquals(expectedProperty.getComment(), actualProperty.getComment());
        assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        assertEquals(expectedProperty.getPattern(), actualProperty.getPattern());
        assertEquals(expectedProperty.isPassword(), actualProperty.isPassword());
    }
}