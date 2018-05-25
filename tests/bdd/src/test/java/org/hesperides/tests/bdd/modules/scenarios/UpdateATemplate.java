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

public class UpdateATemplate extends CucumberSpringBean implements En {

    @Autowired
    private ExistingTemplateContext existingTemplateContext;
    @Autowired
    private ExistingModuleContext existingModuleContext;
    @Autowired
    private TemplateSample templateSample;

    private ResponseEntity response;

    public UpdateATemplate() {

        When("^updating this template$", () -> {
            response = updateTemplate(false);
        });

        When("^updating the same template at the same time$", () -> {
            response = updateTemplate(true);
        });

        Then("^the template is successfully updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            templateSample.assertTemplateProperties(templateOutput, existingModuleContext.getNamespace(), 2L);
            assertEquals(2L, templateOutput.getVersionId().longValue());
            //TODO Tester le reste par rapport à l'input
        });

        Then("^the template update is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private ResponseEntity updateTemplate(boolean isGoingToThrowAnError) {
        ResponseEntity response;
        TemplateIO templateInput = templateSample.getTemplateInput(1);
        if (isGoingToThrowAnError) {
            response = existingTemplateContext.failTryingToUpdateModuleTemplate(templateInput);
        } else {
            existingTemplateContext.updateModuleTemplate(templateInput);
            response = existingTemplateContext.getExistingTemplate();
        }
        return response;
    }

    /**
     * TODO Tester la tentative de modification d'un template qui n'existe pas => 404
     */
}
