package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hesperides.tests.bdd.SpringIntegrationTest;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteATemplate extends SpringIntegrationTest {

    @Autowired
    private ExistingTemplateContext existingTemplateContext;

    @When("^deleting this template$")
    public void deletingThisTemplate() throws Throwable {
        template.delete(existingTemplateContext.getTemplateLocation());
    }

    @Then("^the template is successfully deleted$")
    public void theTemplateIsSuccessfullyDeleted() throws Throwable {
        ResponseEntity<String> entity = template.doWithErrorHandlerDisabled(template1 ->
                template1.getForEntity(existingTemplateContext.getTemplateLocation(), String.class));
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
