package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ReleaseAModule extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleView> response;

    @Autowired
    private ExistingModuleContext existingModule;

    public ReleaseAModule() {
        When("^releasing the module$", () -> {
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            response = rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}&release_version={releaseVersion}",
                    null,
                    ModuleView.class,
                    moduleKey.getName(), moduleKey.getVersion(), "1.0.0");
        });

        Then("^the module is released$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleView module = response.getBody();
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            assertEquals(moduleKey.getName(), module.getName());
            assertEquals("1.0.0", module.getVersion());
            assertEquals(false, module.isWorkingCopy());
            //TODO technos et templates ?
            assertEquals(1, module.getVersionId().longValue());
        });
    }
}
