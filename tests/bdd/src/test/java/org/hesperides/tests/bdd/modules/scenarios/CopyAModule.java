package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableList;
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

public class CopyAModule extends CucumberSpringBean implements En {

    private ModuleIO moduleInput;
    private ResponseEntity<ModuleIO> response;

    @Autowired
    private ExistingModuleContext existingModule;

    public CopyAModule() {
        When("^creating a copy of this module$", () -> {
            moduleInput = new ModuleIO("test", "1.0.1", true, ImmutableList.of(), 0L);
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            response = rest.getTestRest().postForEntity("/modules?from_module_name={moduleName}&from_module_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                    moduleInput, ModuleIO.class,
                    moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
        });

        Then("^the module is successfully duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            assertEquals(moduleInput.getName(), moduleOutput.getName());
            assertEquals(moduleInput.getVersion(), moduleOutput.getVersion());
            assertEquals(moduleInput.isWorkingCopy(), moduleOutput.isWorkingCopy());
            assertTrue(CollectionUtils.isEmpty(moduleOutput.getTechnos()));
            assertEquals(1L, moduleOutput.getVersionId().longValue());
        });

        /**
         * TODO Tester la copie d'un module qui a des technos et templates
         * Si lors d'une copie, on envoie des clés de technos dans le body, elles sont ignorés
         */
    }
}
