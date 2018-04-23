package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class UpdateAModule extends CucumberSpringBean implements En {

    private URI moduleLocation;
    private Exception exception;

    @Autowired
    private ExistingModuleContext existingModuleContext;

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
            ResponseEntity<ModuleView> responseEntity = rest.getForEntity(moduleLocation, ModuleView.class);
            assertEquals(2L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });

        Then("^the module update is rejected$", () -> {
            assertNotNull(exception);
        });
    }

    private void updateModule() {
        Module.Key existingModuleKey = existingModuleContext.getModuleKey();
        ModuleInput moduleInput = new ModuleInput(existingModuleKey.getName(), existingModuleKey.getVersion(), existingModuleKey.isWorkingCopy(), ImmutableSet.of(), 1L);
        moduleLocation = rest.putForLocationReturnAbsoluteURI("/modules", moduleInput);
    }
}
