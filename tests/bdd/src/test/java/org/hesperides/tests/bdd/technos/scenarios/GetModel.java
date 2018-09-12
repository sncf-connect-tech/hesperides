package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.PropertyAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GetModel implements En {

    @Autowired
    private TechnoContext technoContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<ModelOutput> response;
    private ResponseEntity failResponse;

    public GetModel() {
        Given("^a template in this techno that has properties$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{foo|@required @comment content of template-a @pattern * @password }}")
                    .build());
        });

        When("^retrieving the model of this techno$", () -> {
            response = rest.getTestRest().getForEntity(technoContext.getTechnoURI() + "/model", ModelOutput.class);
        });

        Then("^the model of this techno contains all the properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", true, "content", "", "*", true, null),
                    new ArrayList<>(modelOutput.getProperties()).get(0));
        });

        Given("^a template in this techno that has properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withFilename("{{ foo | @default filename of template-a}}.json")
                    .withLocation("/{{foo|@comment \"location of template-a\"}}")
                    .withContent("{{foo|@required @comment content of template-a @pattern * @password }}")
                    .build());
        });

        Then("^the model of this techno contains all the properties with the same name from this template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(3, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", false, null, "filename", "", false, null)));
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", false, "location of template-a", "", "", false, null)));
            assertEquals(true, modelOutput.getProperties().contains(new PropertyOutput("foo", true, "content", "", "*", true, null)));
        });

        Given("^templates in this techno that have properties with the same name but different attributes$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{ foo | @comment content of template-a @default 12 @pattern * @password }}")
                    .build());
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-b")
                    .withContent("{{foo|@comment \"content of template-b\" }}")
                    .build());
        });

        Then("^the model of this techno contains all the properties with the same name from these templates$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content", "12", "*", true, null)));
            assertEquals(true, modelOutput.getProperties().contains(
                    new PropertyOutput("foo", false, "content of template-b", "", "", false, null)));
        });

        Given("^a template in this techno containing properties that have been updated$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{foo|@required|@comment content of template-a|@pattern *|@password }}")
                    .build());
            technoContext.updateTemplate(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{ foo }}")
                    .withVersionId(1)
                    .build());
        });

        Then("^the model of this techno contains the updated properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(1, modelOutput.getProperties().size());
            PropertyAssertions.assertProperty(new PropertyOutput("foo", false, "", "", "", false, Collections.emptySet()),
                    new ArrayList<>(modelOutput.getProperties()).get(0));
        });

        Given("^a template in this techno containing properties but that is being deleted$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{foo|@required|@comment content of template-a|@pattern *|@password }}")
                    .build());
            technoContext.deleteTemplate("template-a");
        });

        Then("^the model of this techno does not contain the properties of the deleted template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals(0, modelOutput.getProperties().size());
        });

        Given("^a template in this techno that has iterable properties$", () -> {
            technoContext.addTemplateToExistingTechno(new TemplateBuilder()
                    .withName("template-a")
                    .withContent("{{#it1}}{{foo}}{{#it2}}{{bar}}{{/it2}}{{/it1}}")
                    .build());
        });

        Then("^the model of this techno contains all the iterable properties$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModelOutput modelOutput = response.getBody();
            assertEquals("it1", new ArrayList<>(modelOutput.getIterableProperties()).get(0).getName());
            assertEquals("foo", new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(0).getName());
            assertEquals("it2", new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(1).getName());
            assertEquals("bar", new ArrayList<>(new ArrayList<>(new ArrayList<>(modelOutput.getIterableProperties()).get(0).getProperties()).get(1).getProperties()).get(0).getName());
        });

        When("^trying to create a template in this techno that has a property that is required and with a default value$", () -> {
            failResponse = failTryingToCreateTemplate(new TemplateBuilder()
                    .withName("another template")
                    .withContent("{{foo|@required @default 12}}")
                    .build());
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