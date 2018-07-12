package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetModuleTypes extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;

    private ResponseEntity<String[]> response;

    public GetModuleTypes() {

        When("^retrieving the module's types$", () -> {
            response = getModuleTypes();
        });

        Then("^the module's types are workingcopy and release$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> types = Arrays.asList(response.getBody());
            assertEquals(2, types.size());
            assertEquals("workingcopy", types.get(0));
            assertEquals("release", types.get(1));
        });
    }

    private ResponseEntity<String[]> getModuleTypes() {
        TemplateContainer.Key moduleKey = moduleContext.getModuleKey();
        return rest.getTestRest().getForEntity("/modules/{moduleName}/{moduleVersion}", String[].class, moduleKey.getName(), moduleKey.getVersion());
    }
}
