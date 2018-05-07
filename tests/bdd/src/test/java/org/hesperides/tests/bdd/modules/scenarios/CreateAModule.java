package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CreateAModule extends CucumberSpringBean implements En {

    private ModuleIO moduleInput;
    private ResponseEntity<ModuleIO> response;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = new ModuleIO("test", "1.0.0", true, ImmutableList.of(), 0L);
        });

        When("^creating a new module$", () -> {
            response = rest.getTestRest().postForEntity("/modules", moduleInput, ModuleIO.class);
        });

        Then("^the module is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            assertEquals(moduleInput.getName(), moduleOutput.getName());
            assertEquals(moduleInput.getVersion(), moduleOutput.getVersion());
            assertEquals(moduleInput.isWorkingCopy(), moduleOutput.isWorkingCopy());
            assertTrue(CollectionUtils.isEmpty(moduleOutput.getTechnos()));
            assertEquals(1L, moduleOutput.getVersionId().longValue());
        });
    }
}
