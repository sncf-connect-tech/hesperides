package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetTemplates extends CucumberSpringBean implements En {

    @Autowired
    private ExistingTemplateContext existingTemplateContext;

    private ResponseEntity<PartialTemplateIO[]> response;

    public GetTemplates() {

        When("^retrieving those templates$", () -> {
            response = existingTemplateContext.getModuleTemplates();
        });

        Then("^the templates are retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<PartialTemplateIO> templateOutputs = Arrays.asList(response.getBody());
            List<TemplateIO> templateInputs = existingTemplateContext.getTemplateInputs();
            assertEquals(templateInputs.size(), templateOutputs.size());
            //TODO Vérifier le contenu de chaque template ?
        });
    }
}
