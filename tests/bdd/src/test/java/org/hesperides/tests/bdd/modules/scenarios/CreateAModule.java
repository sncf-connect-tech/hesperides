package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class CreateAModule extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;

    public CreateAModule() {
        Given("^a module working copy to create$", () -> {
            moduleInput = new ModuleInput("test", "1.0.0", true, ImmutableSet.of(), 0L);
        });

        When("^creating a new module working copy$", () -> {
            moduleLocation = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        });

        Then("^the module is successfully created$", () -> {
            ResponseEntity<ModuleView> response = rest.getTestRest().getForEntity(moduleLocation, ModuleView.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleView module = response.getBody();
            assertEquals(1L, module.getVersionId().longValue());
            //TODO Tester les propriétés par rapport à l'existant
        });
    }
}
