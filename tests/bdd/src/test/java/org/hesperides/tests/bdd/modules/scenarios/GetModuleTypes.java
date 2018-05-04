package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetModuleTypes extends CucumberSpringBean implements En {

    private ResponseEntity<String[]> response;

    @Autowired
    private ExistingModuleContext existingModule;

    public GetModuleTypes() {

        When("^retrieving the module's types$", () -> {
            response = rest.getTestRest().getForEntity("/modules/{moduleName}/{moduleVersion}", String[].class,
                    existingModule.getModuleKey().getName(), existingModule.getModuleKey().getVersion());
        });

        Then("^the module's types are retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> types = Arrays.asList(response.getBody());
            assertEquals(2, types.size());
            assertEquals("workingcopy", types.get(0));
            assertEquals("release", types.get(1));
        });
    }
}
