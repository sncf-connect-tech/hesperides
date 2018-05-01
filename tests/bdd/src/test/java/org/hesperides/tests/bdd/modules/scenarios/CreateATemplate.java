package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class CreateATemplate extends CucumberSpringBean implements En {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    private ExistingModuleContext existingModule;

    public CreateATemplate() {
        Given("^a template to create$", () -> {
            RightsInput.FileRights rights = new RightsInput.FileRights(true, true, true);
            templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                    new RightsInput(rights, rights, rights), 0L);
        });

        When("^adding a new template$", () -> {
            templateLocation = rest.postForLocationReturnAbsoluteURI(existingModule.getModuleLocation() + "/templates", templateInput);
        });

        Then("^the template is successfully created and the module contains the new template$", () -> {
            ResponseEntity<TemplateView> response = rest.getTestRest().getForEntity(templateLocation, TemplateView.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            //TODO Vérifier le reste
            assertEquals(1L, response.getBody().getVersionId().longValue());
        });
    }
}
