package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetModuleInfo extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleIO> response;

    @Autowired
    private ExistingModuleContext existingModule;

    public GetModuleInfo() {

        When("^retrieving the module's info$", () -> {
            response = rest.getTestRest().getForEntity(existingModule.getModuleLocation(), ModuleIO.class);
        });

        Then("^the module's info is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            assertEquals(moduleKey.getName(), moduleOutput.getName());
            assertEquals(moduleKey.getVersion(), moduleOutput.getVersion());
            assertEquals(moduleKey.isWorkingCopy(), moduleOutput.isWorkingCopy());
            assertEquals(1, moduleOutput.getVersionId().longValue());
            assertTrue(CollectionUtils.isEmpty(moduleOutput.getTechnos()));
        });
    }

    /**
     * TODO Tester un module avec des technos
     */
}
