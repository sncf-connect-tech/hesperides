package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CopyAModule extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleView> response;

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public CopyAModule() {
        When("^creating a copy of this module$", () -> {
            ModuleInput newModule = new ModuleInput("test", "1.0.1", true, ImmutableSet.of(), 0L);
            response = rest.getTestRest().postForEntity(String.format("/modules?from_module_name=%s&from_module_version=%s&from_is_working_copy=%s",
                    existingModuleContext.getModuleKey().getName(),
                    existingModuleContext.getModuleKey().getVersion(),
                    existingModuleContext.getModuleKey().isWorkingCopy()),
                    newModule,
                    ModuleView.class);
        });

        Then("^the module is successfully duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(1L, response.getBody().getVersionId().longValue());
            //TODO Vérifier le reste
        });
    }
}
