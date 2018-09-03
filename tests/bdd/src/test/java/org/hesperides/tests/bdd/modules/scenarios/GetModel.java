package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.PropertyAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GetModel extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private TemplateContext templateContext;
    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity<ModelOutput> response;
    private ResponseEntity failResponse;

    public GetModel() {
        Given("^a template in this module that has properties$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent(
                    "template-a",
                    "{{foo2|@required @comment content of template-a @pattern * @password }}"));
        });

        When("^retrieving the model of this module$", () -> {
            response = rest.getTestRest().getForEntity(moduleContext.getModuleURI() + "/model", ModelOutput.class);
        });

        Then("^the model of this module contains all the properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo2", true, "content", "", "*", true, null),
                    new ArrayList<>(modelOutput.getProperties()).get(0));
        });

        Then("^the model of this module contains all the properties of the techno$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", true, "content", "", "*", true, null),
                    new ArrayList<>(modelOutput.getProperties()).get(0));
        });

        Given("^a template in this module that has properties with the same name but different attributes$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameFilenameLocationAndContent(
                    "template-a",
                    "{{ foo | @default filename of template-a}}.json",
                    "/{{foo|@comment \"location of template-a\"}}",
                    "{{foo|@required @comment content of template-a @pattern * @password }}"));
        });

        Then("^the model of this module contains all the properties with the same name from this template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(3, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", false, null, "filename", "", false, null)));
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", false, "location of template-a", "", "", false, null)));
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", true, "content", "", "*", true, null)));
        });

        Given("^templates in this module that have properties with the same name but different attributes$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{ foo | @comment content of template-a @default 12 @pattern * @password }}"));
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent("template-b",
                    "{{foo|@comment \"content of template-b\" }}"));
        });

        Then("^the model of this module contains all the properties with the same name from these templates$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content", "12", "*", true, null)));
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content of template-b", "", "", false, null)));
        });

        Given("^a template in this module containing properties that have been updated$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@pattern *|@password }}"));
            templateContext.updateTemplate(TemplateSamples.getTemplateInputWithNameContentContentAndVersionId("template-a", "{{ foo }}", 1));
        });

        Then("^the model of this module contains the updated properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", false, "", "", "", false, null),
                    new ArrayList<>(modelOutput.getProperties()).get(0));
        });

        Given("^a template in this module containing properties but that is being deleted$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent("template-a",
                    "{{foo|@required|@comment content of template-a|@pattern *|@password }}"));
            templateContext.deleteTemplate("template-a");
        });

        Then("^the model of this module does not contain the properties of the deleted template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(0, modelOutput.getProperties().size());
        });

        Given("^a template in this module that has iterable properties$", () -> {
            templateContext.addTemplateToExistingModule(TemplateSamples.getTemplateInputWithNameAndContent("template-a", "{{#it1}}{{foo}}{{#it2}}{{bar}}{{/it2}}{{/it1}}"));
        });

        Then("^the model of this module contains all the iterable properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals("it1", new ArrayList<>(modelOutput.getIterableProperties()).get(0).getName());
            assertEquals("foo", new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(0).getName());
            assertEquals("it2", new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(1).getName());
            assertEquals("bar", new ArrayList<>(new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(1).getProperties()).get(0).getName());
        });

        When("^trying to create a template in this module that has a property that is required and with a default value$", () -> {
            failResponse = failTryingToCreateTemplate(TemplateSamples.getTemplateInputWithNameAndContent("another-template", "{{foo|@required @default 12}}"));
        });

        Then("^the creation of the module template that has a property that is required and with a default value is rejected$", () -> {
            assertEquals(HttpStatus.BAD_REQUEST, failResponse.getStatusCode());
        });

        When("^the techno is updated with new properties", () -> {
            TemplateIO templateInput = TemplateSamples.getTemplateInputWithContentAndVersionId("{{a}}{{b}}{{c}}{{d}}", 1);
            technoContext.updateTemplate(templateInput);
        });

        Then("^the model of this module contains the new properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(5, modelOutput.getProperties().size());
        });

        When("^the techno's template is deleted$", () -> {
            technoContext.deleteTemplate("template-a");
        });

        Then("^the model of this module does not contain the properties of the deleted techno template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(0, modelOutput.getProperties().size());
        });

        When("^a new template is added to this techno$", () -> {
            TemplateIO templateInput = TemplateSamples.getTemplateInputWithNameAndContent("another template", "{{a}}{{b}}{{c}}{{d}}");
            technoContext.addTemplateToExistingTechno(templateInput);
        });

        When("^the techno is deleted$", () -> {
            technoContext.deleteTechno();
        });

        Then("^the model of this module does not contain the properties of the deleted techno$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(0, modelOutput.getProperties().size());
        });
    }

    private ResponseEntity<String> failTryingToCreateTemplate(TemplateIO templateInput) {
        return rest.doWithErrorHandlerDisabled(rest -> rest.postForEntity(
                templateContext.getTemplatesURI(), templateInput, String.class));
    }

    //TODO Mieux répartir ces tests (features et méthodes)
}