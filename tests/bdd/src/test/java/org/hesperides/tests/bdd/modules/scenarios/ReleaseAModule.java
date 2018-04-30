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
    private ExistingModuleContext existingModuleContext;

    public ReleaseAModule() {
        When("^releasing the module$", () -> {
            TemplateContainer.Key existingModuleKey = existingModuleContext.getModuleKey();
            response = rest.getTestRest().postForEntity(String.format("/modules/create_release?module_name=%s&module_version=%s&release_version=%s",
                    existingModuleKey.getName(),
                    existingModuleKey.getVersion(),
                    "1.0.0"),
                    null,
                    ModuleView.class);
        });

        Then("^the module is released$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleView module = response.getBody();
            TemplateContainer.Key existingModuleKey = existingModuleContext.getModuleKey();
            assertEquals(existingModuleKey.getName(), module.getName());
            assertEquals("1.0.0", module.getVersion());
            assertEquals(false, module.isWorkingCopy());
            //TODO technos et templates ?
            assertEquals(1, module.getVersionId().longValue());
        });
    }
}
