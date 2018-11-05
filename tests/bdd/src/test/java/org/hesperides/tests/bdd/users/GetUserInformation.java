package org.hesperides.tests.bdd.users;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetUserInformation extends HesperidesScenario implements En {

    @Autowired
    private RestTemplate restTemplate;

    public GetUserInformation() {

        When("^I get the current user information$", () -> {
            testContext.responseEntity = restTemplate.getForEntity("/users/auth", Map.class);
        });

        Then("^the user information is provided$", () -> {
            assertEquals(HttpStatus.OK, testContext.responseEntity.getStatusCode());
            Map map = getBodyAsMap();
            assertEquals("user", map.get("username"));
            assertEquals(false, map.get("prodUser"));
            assertEquals(false, map.get("techUser"));
        });
    }
}
