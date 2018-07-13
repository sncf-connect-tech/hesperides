package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.ModuleSamples;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetModuleVersions extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;

    private ResponseEntity<String[]> response;

    public GetModuleVersions() {

        Given("^an existing module with multiple versions$", () -> {
            for (int i = 0; i < 6; i++) {
                ModuleIO moduleInput = ModuleSamples.getModuleInputWithNameAndVersion("test", "1.0." + i);
                moduleContext.createModule(moduleInput);
            }
        });

        When("^retrieving the module's versions$", () -> {
            response = getModuleVersions();
        });

        Then("^the module's versions are retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> versions = Arrays.asList(response.getBody());
            assertEquals(6, versions.size());
        });
    }

    private ResponseEntity<String[]> getModuleVersions() {
        TemplateContainer.Key moduleKey = moduleContext.getModuleKey();
        return rest.getTestRest().getForEntity("/modules/{moduleName}", String[].class, moduleKey.getName());
    }
}
