package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TechnoInput;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATechno extends CucumberSpringBean implements En {
    private TechnoInput technoInput;
    private ResponseEntity<TemplateView> response;

    public CreateATechno() {
        Given("^a techno to create$", () -> {
            technoInput = new TechnoInput(
                    "technoName",
                    "technoVersion",
                    true,
                    new TemplateInput(
                            "fichierTest",
                            "test.json",
                            "/home/test",
                            "{test:test}",
                            new RightsInput(
                                    new RightsInput.FileRights(null, null, null),
                                    new RightsInput.FileRights(null, null, null),
                                    null
                            ), -1L));
        });

        When("^creating a new techno$", () -> {
            response = rest.getTestRest().postForEntity(
                    "/templates/packages/{technoName}/{technoVersion}/workingcopy/templates",
                    technoInput.getTemplate(),
                    TemplateView.class,
                    technoInput.getName(),
                    technoInput.getVersion());
        });


        Then("^the techno is successfully created$", () -> {
            TemplateView template = response.getBody();
            assertEquals(1L, template.getVersionId().longValue());
            assertEquals("packages#technoName#technoVersion#WORKINGCOPY", template.getNamespace());
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        });
    }

}
