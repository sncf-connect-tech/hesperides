package org.hesperides.tests.bdd.versions.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetVersions implements En {

    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<Map> response;

    public GetVersions() {

        When("^retrieving the application versions$", () -> {
            response = rest.getTestRest().getForEntity("/versions", Map.class);
        });

        Then("^the versions are not empty$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map map = response.getBody();
            assertNotNull(map.get("backend_version"));
            assertNotNull(map.get("api_version"));
        });
    }
}
