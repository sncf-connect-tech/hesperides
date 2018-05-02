package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateAModule extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = new ModuleInput("test", "1.0.0", true, ImmutableSet.of(), 0L);
        });

        When("^creating a new module$", () -> {
            moduleLocation = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        });

        Then("^the module is successfully created$", () -> {
            ResponseEntity<ModuleView> responseEntity = rest.getTestRest().getForEntity(moduleLocation, ModuleView.class);
            assertEquals(1L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
            //TODO Tester les propriétés par rapport à l'existant
        });
    }
}
