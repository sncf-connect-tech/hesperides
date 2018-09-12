package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ReleaseAModule implements En {

    @Autowired
    private ModuleContext moduleContext;

    private ResponseEntity<ModuleIO> response;

    public ReleaseAModule() {
        When("^releasing the module$", () -> {
            response = moduleContext.releaseModule();
        });

        Then("^the module is released$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            TemplateContainer.Key moduleKey = moduleContext.getModuleKey();
            assertEquals(moduleKey.getName(), moduleOutput.getName());
            assertEquals(moduleKey.getVersion(), moduleOutput.getVersion());
            assertEquals(false, moduleOutput.isWorkingCopy());
            assertEquals(1, moduleOutput.getVersionId().longValue());
        });
    }

    // TODO Tester avec technos et templates (copie profonde)
}
