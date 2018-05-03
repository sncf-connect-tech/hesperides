package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class UpdateAModule extends CucumberSpringBean implements En {

    private ResponseEntity response;

    @Autowired
    private ExistingModuleContext existingModule;

    public UpdateAModule() {

        When("^updating this module$", () -> {
            updateModule(false);
        });

        When("^updating the same version of the module alongside$", () -> {
            updateModule(true);
        });

        Then("^the module is successfully updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleIO module = (ModuleIO) response.getBody();
            assertEquals(2L, module.getVersionId().longValue());
            // TODO Tester le reste ? Est-ce que ça a un intérêt ?
        });

        Then("^the module update is rejected$", () -> {
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        });
    }

    private void updateModule(boolean isGoingToThrowAnError) {
        Module.Key moduleKey = existingModule.getModuleKey();
        ModuleIO moduleInput = new ModuleIO(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy(), ImmutableList.of(), 1L);

        if (isGoingToThrowAnError) {
            response = rest.doWithErrorHandlerDisabled(rest -> rest.exchange("/modules", HttpMethod.PUT, new HttpEntity<>(moduleInput), String.class));
        } else {
            response = rest.putForEntity("/modules", moduleInput, ModuleIO.class);
        }
    }

    /**
     * TODO Tester la mise à jour de technos
     */
}
