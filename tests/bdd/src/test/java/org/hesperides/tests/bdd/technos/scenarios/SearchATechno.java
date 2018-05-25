package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchATechno extends CucumberSpringBean implements En {

    private ResponseEntity<TechnoIO[]> response;

    public SearchATechno() {

        When("^searching for a specific techno$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=technoName-6 technoVersion-6", null, TechnoIO[].class);
        });

        Then("^the techno is found$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<TechnoIO> technos = Arrays.asList(response.getBody());
            TechnoIO techno = technos.get(0);
            assertEquals(1, technos.size());
            assertEquals("technoName-6", techno.getName());
            assertEquals("technoVersion-6", techno.getVersion());
            assertEquals(true, techno.isWorkingCopy());
        });

        When("^searching for some of those technos$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=technoName", null, TechnoIO[].class);
        });

        Then("^the number of techno results is limited$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<TechnoIO> modules = Arrays.asList(response.getBody());
            assertEquals(10, modules.size());
        });

        When("^searching for a techno that does not exist$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=foo", null, TechnoIO[].class);
        });

        Then("^the techno results is empty$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<TechnoIO> modules = Arrays.asList(response.getBody());
            assertEquals(0, modules.size());
        });
    }
}
