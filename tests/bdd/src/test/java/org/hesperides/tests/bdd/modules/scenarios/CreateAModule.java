package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.modules.ModuleAssertions;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateAModule implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private TechnoContext technoContext;

    private ModuleIO moduleInput;
    private ResponseEntity<ModuleIO> response;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = new ModuleBuilder().build();
        });

        When("^creating a new module$", () -> {
            response = moduleContext.createModule(moduleInput);
        });

        Then("^the module is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            ModuleAssertions.assertModule(moduleInput, moduleOutput, 1L);
        });

        When("^creating a module that has the same key as this techno$", () -> {
            TemplateContainer.Key technoKey = technoContext.getTechnoKey();
            moduleInput = new ModuleIO(technoKey.getName(), technoKey.getVersion(), technoKey.isWorkingCopy(), null, 0L);
            response = moduleContext.createModule(moduleInput);
        });
    }
}
