package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hesperides.presentation.controllers.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAModule extends CucumberSpringBean {

    private ModuleInput moduleInput;
    private URI moduleLocation;

    @Given("^a module to create$")
    public void aModuleToCreate() {
        moduleInput = new ModuleInput("test", "123", true, ImmutableSet.of(), 1L);
    }

    @When("^creating a new module$")
    public void creatingANewModule() {
        moduleLocation = template.postForLocationReturnAbsoluteURI("/modules", moduleInput);
    }

    @Then("^the module is successfully created$")
    public void theModuleIsSuccessfullyCreated() {
        ResponseEntity<String> responseEntity = template.getForEntity(moduleLocation, String.class);
        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

}
