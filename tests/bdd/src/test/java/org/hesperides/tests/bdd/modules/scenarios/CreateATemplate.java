package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateATemplate extends CucumberSpringBean implements En {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public CreateATemplate() {
        Given("^a template to create$", () -> {
            RightsInput.FileRights rights = new RightsInput.FileRights(true, true, true);
            templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                    new RightsInput(rights, rights, rights), 0L);
        });

        When("^adding a new template$", () -> {
            templateLocation = rest.postForLocationReturnAbsoluteURI(existingModuleContext.getModuleLocation().toString() + "/templates", templateInput);
        });

        Then("^the template is successfully created and the module contains the new template$", () -> {
            ResponseEntity<TemplateView> responseEntity = rest.getForEntity(templateLocation, TemplateView.class);
            assertEquals(1L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
