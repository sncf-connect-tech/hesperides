package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.hesperides.tests.bdd.templatecontainer.tools.TemplateAssertion;
import org.hesperides.tests.bdd.templatecontainer.tools.TemplateSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetATemplate extends CucumberSpringBean implements En {

    @Autowired
    private ExistingTemplateContext existingTemplateContext;
    @Autowired
    private ExistingModuleContext existingModuleContext;

    private ResponseEntity<TemplateIO> response;

    public GetATemplate() {

        When("^retrieving this module template$", () -> {
            response = existingTemplateContext.getExistingTemplate();
        });

        Then("^the module template is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertion.assertTemplateProperties(templateOutput, existingModuleContext.getNamespace(), 1);
        });
    }

    /**
     * TODO Tester la tentative de récupération d'un template qui n'existe pas
     */
}
