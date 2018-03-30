package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.controllers.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateAModule extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public CreateAModule() {
        Given("^a module to create$", () -> {
            moduleInput = new ModuleInput("test", "123", true, ImmutableSet.of(), 0L);
        });

        When("^creating a new module$", () -> {
            moduleLocation = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        });

        When("^creating a copy of this module$", () -> {
            ModuleInput newModule = new ModuleInput("test2", "123", true, ImmutableSet.of(), 0L);
            moduleLocation = rest.postForLocationReturnAbsoluteURI(String.format("/modules?from_module_name=%s&from_module_version=%s&from_is_working_copy=%s",
                    existingModuleContext.getModuleKey().getName(),
                    existingModuleContext.getModuleKey().getVersion(),
                    existingModuleContext.getModuleKey().isWorkingCopy()),
                    newModule);
        });

        Then("^the module is successfully created$", () -> {
            ResponseEntity<ModuleView> responseEntity = rest.getForEntity(moduleLocation, ModuleView.class);
            assertEquals(1L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }
}
