package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.presentation.controllers.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateATemplate extends CucumberSpringBean implements En {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    private ExistingModuleContext moduleContext;

    public CreateATemplate() {
        Given("^a template to create$", () -> {
            Template.FileRights rights = new Template.FileRights(true, true, true);
            templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                    new Template.Rights(rights, rights, rights));
        });

        When("^adding a new template$", () -> {
            templateLocation = template.postForLocationReturnAbsoluteURI(moduleContext.getModuleLocation().toString() + "/templates", templateInput);
        });

        Then("^the module contains the new template$", () -> {
            ResponseEntity<String> responseEntity = template.getForEntity(templateLocation, String.class);
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
