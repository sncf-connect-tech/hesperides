package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.presentation.controllers.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateATemplate extends CucumberSpringBean implements En {

    private URI templateLocation;

    @Autowired
    private ExistingTemplateContext existingTemplateContext;

    public UpdateATemplate() {
        When("^updating this template$", () -> {
            Template.FileRights rights = new Template.FileRights(true, true, true);
            TemplateInput templateInput = new TemplateInput("templateName", "template.name", "template.location", "content-bis",
                    new Template.Rights(rights, rights, rights));
            Module.Key moduleKey = existingTemplateContext.getExistingModuleContext().getModuleKey();
            templateLocation = rest.putForLocationReturnAbsoluteURI(
                    "/modules/{id}/{version}/workingcopy/templates/",
                    templateInput,
                    moduleKey.getName(),
                    moduleKey.getVersion());
        });

        Then("^the template is successfully updated", () -> {
            ResponseEntity<String> responseEntity = rest.getForEntity(templateLocation, String.class);
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
