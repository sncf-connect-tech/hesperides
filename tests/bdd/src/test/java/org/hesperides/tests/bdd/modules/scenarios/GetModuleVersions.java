package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetModuleVersions extends CucumberSpringBean implements En {

    private ResponseEntity<String[]> response;

    public GetModuleVersions() {

        When("^retrieving the module's versions$", () -> {
            response = rest.getTestRest().getForEntity("/modules/test", String[].class);
        });

        Then("^the module's versions are retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> versions = Arrays.asList(response.getBody());
            assertEquals(6, versions.size());
            //TODO released
        });
    }
}
