package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

public class UpdateATemplate extends CucumberSpringBean implements En {

    private ResponseEntity<TemplateIO> response;
    private Exception exception;

    @Autowired
    private ExistingTemplateContext existingTemplate;

    public UpdateATemplate() {
        Given("^this template is being modified alongside$", () -> {
            updateTemplate();
        });

        When("^updating this template$", () -> {
            try {
                updateTemplate();
            } catch (Exception e) {
                exception = e;
            }
        });

        Then("^the template is successfully updated", () -> {
            assertNull(exception);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2L, response.getBody().getVersionId().longValue());
            //TODO Tester le reste par rapport à l'input ?
        });

        Then("^the template update is rejected$", () -> {
            assertNotNull(exception);
        });
    }

    private void updateTemplate() {
        TemplateIO.FileRightsIO rights = new TemplateIO.FileRightsIO(true, true, true);
        TemplateIO templateIO = new TemplateIO(null, "templateName", "template.name", "template.location", "content-bis",
                new TemplateIO.RightsIO(rights, rights, rights), 1L);
        Module.Key moduleKey = existingTemplate.getExistingModuleContext().getModuleKey();
        response = rest.putForEntity("/modules/{moduleName}/{moduleVersion}/workingcopy/templates/",
                templateIO, TemplateIO.class,
                moduleKey.getName(), moduleKey.getVersion());
    }

    /**
     * TODO Tester la tentative de modification d'un template qui n'existe pas => 404
     * TODO Tester la tentative de modification d'un template qui a été modifié entre temps => 409
     */
}
