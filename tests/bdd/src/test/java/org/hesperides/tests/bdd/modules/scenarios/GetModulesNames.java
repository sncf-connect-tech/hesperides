package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GetModulesNames extends CucumberSpringBean implements En {

    @Autowired
    private ModuleContext moduleContext;

    private ResponseEntity<String[]> response;

    public GetModulesNames() {

        When("^listing all modules names$", () -> {
            response = getModulesNames();
        });

        Then("^I get a distinct list of all modules names$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<String> modules = Arrays.asList(response.getBody());
            assertEquals(20, modules.size());
            assertEquals(false, containsDuplicates(modules));
        });
    }

    private ResponseEntity<String[]> getModulesNames() {
        return rest.getTestRest().getForEntity("/modules", String[].class);
    }

    private boolean containsDuplicates(List<String> modules) {
        Set<String> set = new HashSet<>(modules);
        return set.size() < modules.size();
    }
}
