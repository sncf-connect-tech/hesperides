package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class UpdateATemplate extends CucumberSpringBean implements En {

    private ResponseEntity response;

    @Autowired
    private ExistingTemplateContext existingTemplate;

    public UpdateATemplate() {

        When("^updating this template$", () -> {
            updateTemplate(false);
        });

        When("^updating the same version of the template alongside$", () -> {
            updateTemplate(true);
        });

        Then("^the template is successfully updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO template = (TemplateIO) response.getBody();
            assertEquals(2L, template.getVersionId().longValue());
            //TODO Tester le reste par rapport à l'input ?
        });

        Then("^the template update is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private void updateTemplate(boolean isGoingToThrowAnError) {
        TemplateIO.FileRightsIO rightsInput = new TemplateIO.FileRightsIO(true, true, true);
        TemplateIO templateInput = new TemplateIO("templateName", null, "template.name", "template.location", "content-bis",
                new TemplateIO.RightsIO(rightsInput, rightsInput, rightsInput), 1L);
        Module.Key moduleKey = existingTemplate.getExistingModuleContext().getModuleKey();

        if (isGoingToThrowAnError) {
            response = rest.doWithErrorHandlerDisabled(rest -> rest.exchange("/modules/{moduleName}/{moduleVersion}/workingcopy/templates/",
                    HttpMethod.PUT, new HttpEntity<>(templateInput), String.class, moduleKey.getName(), moduleKey.getVersion()));
        } else {
            response = rest.putForEntity("/modules/{moduleName}/{moduleVersion}/workingcopy/templates/", templateInput, TemplateIO.class, moduleKey.getName(), moduleKey.getVersion());
        }
    }

    /**
     * TODO Tester la tentative de modification d'un template qui n'existe pas => 404
     * TODO Tester la tentative de modification d'un template qui a été modifié entre temps => 409
     */
}
