package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetModuleVersions implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<String[]> response;

    public GetModuleVersions() {

        Given("^an existing module with multiple versions$", () -> {
            for (int i = 0; i < 6; i++) {
                ModuleIO moduleInput = new ModuleBuilder()
                        .withName("test")
                        .withVersion("1.0." + i)
                        .build();
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
