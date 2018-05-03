package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.hesperides.tests.bdd.templatecontainer.TemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATemplate extends CucumberSpringBean implements En {

    private TemplateIO templateInput;
    private ResponseEntity response;

    @Autowired
    private ExistingModuleContext existingModule;

    public CreateATemplate() {
        Given("^a template to create$", () -> {
            TemplateIO.FileRightsIO rights = new TemplateIO.FileRightsIO(true, true, true);
            templateInput = new TemplateIO(null, "templateName", "template.name", "template.location", "content",
                    new TemplateIO.RightsIO(rights, rights, rights), 0L);
        });

        When("^adding a new template$", () -> {
            addTemplateToExistingModule(false);
        });

        When("^adding this template twice$", () -> {
            addTemplateToExistingModule(false);
            addTemplateToExistingModule(true);
        });

        Then("^the template is successfully created and the module contains the new template$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            assertEquals("modules#" + existingModule.getModuleKey().getName() + "#" + existingModule.getModuleKey().getVersion() + "#WORKINGCOPY", templateOutput.getNamespace());
            assertEquals(templateInput.getName(), templateOutput.getName());
            assertEquals(templateInput.getFilename(), templateOutput.getFilename());
            assertEquals(templateInput.getLocation(), templateOutput.getLocation());
            assertEquals(templateInput.getContent(), templateOutput.getContent());
            TemplateUtils.assertRights(templateInput.getRights(), templateOutput.getRights());
            assertEquals(1L, templateOutput.getVersionId().longValue());
        });

        Then("^the second one is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private void addTemplateToExistingModule(boolean isGoingToThrowAnError) {
        if (isGoingToThrowAnError) {
            response = rest.doWithErrorHandlerDisabled(rest -> rest.postForEntity(existingModule.getModuleLocation() + "/templates", templateInput, String.class));
        } else {
            response = rest.getTestRest().postForEntity(existingModule.getModuleLocation() + "/templates", templateInput, TemplateIO.class);
        }
    }
}
