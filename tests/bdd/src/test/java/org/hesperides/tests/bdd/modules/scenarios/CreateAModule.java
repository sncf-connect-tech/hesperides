package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.ModuleAssertions;
import org.hesperides.tests.bdd.modules.ModuleSamples;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateAModule extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;

    private ModuleIO moduleInput;
    private ResponseEntity<ModuleIO> response;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = ModuleSamples.getModuleInputWithDefaultValues();
        });

        When("^creating a new module$", () -> {
            response = moduleContext.createModule(moduleInput);
        });

        Then("^the module is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            ModuleAssertions.assertModule(moduleInput, moduleOutput, 1L);
        });
    }
}
