package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.controllers.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateAModule extends CucumberSpringBean implements En {

    @Autowired
    private ExistingModuleContext existingModuleContext;

    private URI moduleLocation;

    public UpdateAModule() {
        When("^updating this module$", () -> {
            Module.Key existingModuleKey = existingModuleContext.getModuleKey();
            ModuleInput moduleInput = new ModuleInput(existingModuleKey.getName(), existingModuleKey.getVersion(), existingModuleKey.isWorkingCopy(), ImmutableSet.of(), 2L);
            moduleLocation = rest.putForLocationReturnAbsoluteURI("/modules", moduleInput);
        });

        Then("^the module is successfully updated", () -> {
            ResponseEntity<String> responseEntity = rest.getForEntity(moduleLocation, String.class);
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
