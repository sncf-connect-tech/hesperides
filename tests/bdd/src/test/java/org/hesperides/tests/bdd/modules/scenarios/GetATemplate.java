package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.hesperides.tests.bdd.modules.contexts.TemplateContext;
import org.hesperides.tests.bdd.templatecontainer.TemplateAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetATemplate extends CucumberSpringBean implements En {

    @Autowired
    private ExistingModuleContext existingModuleContext;
    @Autowired
    private TemplateContext templateContext;

    private ResponseEntity<TemplateIO> response;

    public GetATemplate() {

        When("^retrieving this module template$", () -> {
            response = templateContext.retrieveExistingTemplate();
        });

        Then("^the module template is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, existingModuleContext.getNamespace(), 1);
        });
    }

    /**
     * TODO Tester la tentative de récupération d'un template qui n'existe pas
     */
}
