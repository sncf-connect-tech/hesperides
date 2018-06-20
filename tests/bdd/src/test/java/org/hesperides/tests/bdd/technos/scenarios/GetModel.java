package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.PropertyAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetModel extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity<ModelOutput> response;
    private ResponseEntity failResponse;

    public GetModel() {
        Given("^a template in this techno that has properties$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent(
                    "template-a",
                    "{{foo|@required @comment content of template-a @pattern * @password }}"));
        });

        When("^retrieving the model of this techno$", () -> {
            response = rest.getTestRest().getForEntity(technoContext.getTechnoURI() + "/model", ModelOutput.class);
        });

        Then("^the model of this techno contains all the properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", true, "content of template-a", "", "*", true, null),
                    modelOutput.getProperties().get(0));
        });

        Given("^a template in this techno that has properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameFilenameLocationAndContent(
                    "template-a",
                    "{{ foo | @comment filename of template-a}}.json",
                    "/{{foo|@comment \"location of template-a\"}}",
                    "{{foo|@required @comment content of template-a @pattern * @password }}"));
        });

        Then("^the model of this techno contains all the properties with the same name from this template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(3, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", false, "filename of template-a", "", "", false, null),
                    modelOutput.getProperties().get(0));
            PropertyAssertions.assertProperty(new PropertyOutput("foo", false, "location of template-a", "", "", false, null),
                    modelOutput.getProperties().get(1));
            PropertyAssertions.assertProperty(new PropertyOutput("foo", true, "content of template-a", "", "*", true, null),
                    modelOutput.getProperties().get(2));
        });

        Given("^templates in this techno that have properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{ foo | @comment content of template-a @default 12 @pattern * @password }}"));
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-b",
                    "{{foo|@comment \"content of template-b\" }}"));
        });

        Then("^the model of this techno contains all the properties with the same name from these templates$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content of template-a", "12", "*", true, null)));
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content of template-b", "", "", false, null)));
        });

        Given("^a template in this techno containing properties that have been updated$", () -> {
            technoContext.addTemplateToExistingTechno(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@default 12|@pattern *|@password }}"));
            technoContext.updateTemplate(TemplateSamples.getTemplateInputWithNameContentContentAndVersionId("template-a", "{{ foo }}", 1));
        });

        Then("^the model of this techno contains the updated properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", false, "", "", "", false, null),
                    modelOutput.getProperties().get(0));
        });

        Given("^a template in this techno containing properties but that is being deleted$", () -> {
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
            assertEquals("it2", modelOutput.getIterableProperties().get(0).getProperties().get(1).getName());
            assertEquals("bar", modelOutput.getIterableProperties().get(0).getProperties().get(1).getProperties().get(0).getName());
        });

        When("^trying to create a template in this techno that has a property that is required and with a default value$", () -> {
            failResponse = failTryingToCreateTemplate(TemplateSamples.getTemplateInputWithNameAndContent("another-template", "{{foo|@required @default 12}}"));
        });

        Then("^the creation of the techno template that has a property that is required and with a default value is rejected$", () -> {
            assertEquals(HttpStatus.BAD_REQUEST, failResponse.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToCreateTemplate(TemplateIO templateInput) {
        return rest.doWithErrorHandlerDisabled(rest -> {
            String templateURI = String.format("/templates/packages/%s/%s/workingcopy/templates",
                    technoContext.getTechnoKey().getName(),
                    technoContext.getTechnoKey().getVersion());

            return rest.postForEntity(templateURI, templateInput, String.class);
        });
    }

    //TODO Mieux répartir ces tests (features et méthodes)
}