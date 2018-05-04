package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ReleaseAModule extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleIO> response;

    @Autowired
    private ExistingModuleContext existingModule;

    public ReleaseAModule() {
        When("^releasing the module$", () -> {
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            response = rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}",
                    null, ModuleIO.class,
                    moduleKey.getName(), moduleKey.getVersion());
        });

        Then("^the module is released$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            assertEquals(moduleKey.getName(), moduleOutput.getName());
            assertEquals(moduleKey.getVersion(), moduleOutput.getVersion());
            assertEquals(false, moduleOutput.isWorkingCopy());
            assertEquals(1, moduleOutput.getVersionId().longValue());
        });
    }

    /**
     * TODO Tester avec version de release, technos et templates
     */
}
