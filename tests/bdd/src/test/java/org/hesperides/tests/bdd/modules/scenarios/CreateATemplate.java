package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.hesperides.tests.bdd.templatecontainer.contexts.TemplateSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATemplate extends CucumberSpringBean implements En {
    @Autowired
    private ExistingTemplateContext existingTemplateContext;
    @Autowired
    private ExistingModuleContext existingModuleContext;
    @Autowired
    private TemplateSample templateSample;

    private ResponseEntity response;

    public CreateATemplate() {

        When("^adding a new template to this module$", () -> {
            response = addTemplateToExistingModule(false);
        });

        When("^trying to add the same template to this module$", () -> {
            response = addTemplateToExistingModule(true);
        });

        Then("^the template is successfully created and the module contains the new template$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            templateSample.assertTemplateProperties(templateOutput, existingModuleContext.getNamespace(), 1);
        });

        Then("^the second attempt to add the template to the module is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private ResponseEntity addTemplateToExistingModule(boolean isGoingToThrowAnError) {
        ResponseEntity response;
        if (isGoingToThrowAnError) {
            response = existingTemplateContext.failTryingToAddTemplateToExistingModule();
        } else {
            existingTemplateContext.addTemplateToExistingModule();
            response = existingTemplateContext.getExistingTemplate();
        }
        return response;
    }
}
