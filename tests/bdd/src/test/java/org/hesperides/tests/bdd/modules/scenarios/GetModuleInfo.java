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

public class GetModuleInfo extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleView> response;

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public GetModuleInfo() {

        When("^retrieving the module's info$", () -> {
            TemplateContainer.Key moduleKey = existingModuleContext.getModuleKey();
            response = rest.getTestRest().getForEntity(String.format("/modules/%s/%s/%s", moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType()), ModuleView.class);
        });

        Then("^the module's info is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleView module = response.getBody();
            TemplateContainer.Key moduleKey = existingModuleContext.getModuleKey();
            assertEquals(moduleKey.getName(), module.getName());
            assertEquals(moduleKey.getVersion(), module.getVersion());
            assertEquals(moduleKey.isWorkingCopy(), module.isWorkingCopy());
            assertEquals(1, module.getVersionId().longValue());
            //TODO technos
            //TODO released
        });
    }
}
