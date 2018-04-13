package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.modules.queries.TemplateView;
import org.hesperides.presentation.controllers.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class UpdateATemplate extends CucumberSpringBean implements En {

    private URI templateLocation;
    private Exception exception;

    @Autowired
    private ExistingTemplateContext existingTemplateContext;

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
            ResponseEntity<TemplateView> responseEntity = rest.getForEntity(templateLocation, TemplateView.class);
            assertEquals(2L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });

        Then("^the template update is rejected$", () -> {
            assertNotNull(exception);
        });
    }

    private void updateTemplate() {
        Template.FileRights rights = new Template.FileRights(true, true, true);
        TemplateInput templateInput = new TemplateInput("templateName", "template.name", "template.location", "content-bis",
                new Template.Rights(rights, rights, rights), 1L);
        Module.Key moduleKey = existingTemplateContext.getExistingModuleContext().getModuleKey();
        templateLocation = rest.putForLocationReturnAbsoluteURI(
                "/modules/{id}/{version}/workingcopy/templates/",
                templateInput,
                moduleKey.getName(),
                moduleKey.getVersion());
    }
}
