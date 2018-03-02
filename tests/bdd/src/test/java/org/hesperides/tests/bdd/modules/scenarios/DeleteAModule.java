package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAModule extends CucumberSpringBean {

    @Autowired
    ExistingModuleContext moduleContext;

    @When("^deleting this module$")
    public void deletingThisModule() throws Throwable {
        template.delete(moduleContext.getModuleLocation());
    }

    @Then("^the module is successfully deleted$")
    public void theModuleIsSuccessfullyDeleted() throws Throwable {
        ResponseEntity<String> responseEntity = template.doWithErrorHandlerDisabled(template1 -> template1.getForEntity(moduleContext.getModuleLocation(), String.class));
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
    }
}
