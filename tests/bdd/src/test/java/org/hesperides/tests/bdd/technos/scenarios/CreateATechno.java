package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.templatecontainer.TemplateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATechno extends CucumberSpringBean implements En {

    private String technoName;
    private String technoVersion;
    private TemplateIO templateInput;
    private ResponseEntity<TemplateIO> response;

    public CreateATechno() {
        Given("^a techno to create$", () -> {
            technoName = "technoName";
            technoVersion = "technoVersion";
            templateInput = new TemplateIO(
                    null,
                    "fichierTest",
                    "test.json",
                    "/home/test",
                    "{test:test}",
                    new TemplateIO.RightsIO(
                            new TemplateIO.FileRightsIO(null, null, null),
                            new TemplateIO.FileRightsIO(null, null, null),
                            new TemplateIO.FileRightsIO(null, null, null)
                    ), -1L);
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
            assertEquals("technos#" + technoName + "#" + technoVersion + "#WORKINGCOPY", templateOutput.getNamespace());
            assertEquals(templateInput.getName(), templateOutput.getName());
            assertEquals(templateInput.getFilename(), templateOutput.getFilename());
            assertEquals(templateInput.getLocation(), templateOutput.getLocation());
            assertEquals(templateInput.getContent(), templateOutput.getContent());
            TemplateUtils.assertRights(templateInput.getRights(), templateOutput.getRights());
            assertEquals(1L, templateOutput.getVersionId().longValue());
        });
    }
}
