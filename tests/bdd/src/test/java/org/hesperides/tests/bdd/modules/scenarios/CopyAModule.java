package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
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
    private ExistingModuleContext existingModule;

    public CopyAModule() {
        When("^creating a copy of this module$", () -> {
            ModuleInput newModule = new ModuleInput("test", "1.0.1", true, ImmutableSet.of(), 0L);
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            response = rest.getTestRest().postForEntity("/modules?from_module_name={moduleName}&from_module_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                    newModule,
                    ModuleView.class,
                    moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
        });

        Then("^the module is successfully duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleView module = response.getBody();
            assertEquals(1L, module.getVersionId().longValue());
            //TODO Vérifier le reste
        });
    }
}
