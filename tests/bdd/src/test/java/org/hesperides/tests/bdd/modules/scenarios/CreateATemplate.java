package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.presentation.controllers.TemplateInput;
import org.hesperides.tests.bdd.SpringIntegrationTest;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateATemplate extends SpringIntegrationTest {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    private ExistingModuleContext moduleContext;

    @Given("^a template to create$")
    public void aTemplateToCreate() throws Throwable {
        Template.FileRights rights = new Template.FileRights(true, true, true);
        templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                new Template.Rights(rights, rights, rights));
    }

    @When("^adding a new template$")
    public void addingANewTemplate() throws Throwable {
        templateLocation = template.postForLocationReturnAbsoluteURI(moduleContext.getModuleLocation().toString() + "/templates", templateInput);
    }

    @Then("^the module contains the new template$")
    public void theModuleContainsTheNewTemplate() throws Throwable {
        ResponseEntity<String> responseEntity = template.getForEntity(templateLocation, String.class);
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
