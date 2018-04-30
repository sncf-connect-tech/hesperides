package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchAModule extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleView[]> response;

    public SearchAModule() {

        When("^searching for one of them$", () -> {
            response = rest.getTestRest().postForEntity("/modules/perform_search?terms=test-12 1.0.12", null, ModuleView[].class);
        });

        Then("^it is found$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ModuleView> modules = Arrays.asList(response.getBody());
            assertEquals(1, modules.size());
            assertEquals("test-12", modules.get(0).getName());
            assertEquals("1.0.12", modules.get(0).getVersion());
        });

        When("^searching for some of them$", () -> {
            response = rest.getTestRest().postForEntity("/modules/perform_search?terms=test", null, ModuleView[].class);
        });

        Then("^the number of results is limited$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ModuleView> modules = Arrays.asList(response.getBody());
            assertEquals(10, modules.size());
        });

        When("^searching for one that does not exist$", () -> {
            response = rest.getTestRest().postForEntity("/modules/perform_search?terms=test-1 version-2", null, ModuleView[].class);
        });

        Then("^the result is empty$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ModuleView> modules = Arrays.asList(response.getBody());
            assertEquals(0, modules.size());
        });
    }
}
