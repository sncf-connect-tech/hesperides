package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GetModulesNames extends CucumberSpringBean implements En {

    private ResponseEntity<String[]> response;

    public GetModulesNames() {

        When("^listing all modules names$", () -> {
            response = rest.getTestRest().getForEntity("/modules", String[].class);
        });

        Then("^I get a distinct list of all modules names$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> modules = Arrays.asList(response.getBody());
            assertEquals(20, modules.size());
            assertEquals(false, containsDuplicates(modules));
        });
    }

    private boolean containsDuplicates(List<String> modules) {
        Set<String> set = new HashSet<>(modules);
        return set.size() < modules.size();
    }
}
