package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

public class UpdateAModule extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleIO> response;
    private Exception exception;

    @Autowired
    private ExistingModuleContext existingModule;

    public UpdateAModule() {
        Given("^this module is being modified alongside$", () -> {
            updateModule();
        });

        When("^updating this module$", () -> {
            try {
                updateModule();
            } catch (Exception e) {
                exception = e;
            }
        });

        Then("^the module is successfully updated", () -> {
            assertNull(exception);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2L, response.getBody().getVersionId().longValue());
            // TODO Tester le reste ? Est-ce que ça a un intérêt ?
        });

        Then("^the module update is rejected$", () -> {
            assertNotNull(exception);
            //TODO Doit renvoyer une 412
        });
    }

    private void updateModule() {
        Module.Key moduleKey = existingModule.getModuleKey();
        ModuleIO moduleInput = new ModuleIO(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy(), ImmutableList.of(), 1L);
        response = rest.putForEntity("/modules", moduleInput, ModuleIO.class);
    }

    /**
     * TODO Tester la mise à jour de technos
     */
}
