package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.hesperides.tests.bdd.templatecontainer.TemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetATemplate extends CucumberSpringBean implements En {

    private ResponseEntity<TemplateIO> response;

    @Autowired
    private ExistingTemplateContext existingTemplate;

    public GetATemplate() {

        When("^retrieving this template$", () -> {
            response = rest.getTestRest().getForEntity(existingTemplate.getTemplateLocation(), TemplateIO.class);
        });

        Then("^the template is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateIO templateInput = existingTemplate.getTemplateInput();
            assertEquals(getNamespace(), templateOutput.getNamespace());
            assertEquals(templateInput.getName(), templateOutput.getName());
            assertEquals(templateInput.getFilename(), templateOutput.getFilename());
            assertEquals(templateInput.getLocation(), templateOutput.getLocation());
            assertEquals(templateInput.getContent(), templateOutput.getContent());
            TemplateUtils.assertRights(templateInput.getRights(), templateOutput.getRights());
            assertEquals(1, templateOutput.getVersionId().longValue());
        });
    }

    private String getNamespace() {
        TemplateContainer.Key moduleKey = existingTemplate.getExistingModuleContext().getModuleKey();
        return "modules#" + moduleKey.getName() + "#" + moduleKey.getVersion() + "#" + moduleKey.getVersionType().name().toUpperCase();
    }

    /**
     * TODO Tester la tentative de récupération d'un template qui n'existe pas
     */
}
