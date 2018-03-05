package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.presentation.controllers.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAModule extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = new ModuleInput("test", "123", true, ImmutableSet.of(), 1L);
        });

        When("^creating a new module$", () -> {
            moduleLocation = template.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        });

        Then("^the module is successfully created$", () -> {
            ResponseEntity<String> responseEntity = template.getForEntity(moduleLocation, String.class);
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
