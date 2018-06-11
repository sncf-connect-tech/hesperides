package org.hesperides.tests.bdd.users.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetUserInfo extends CucumberSpringBean implements En {

    private ResponseEntity<Map> response;

    public GetUserInfo() {

        When("^retrieving user info$", () -> {
            response = rest.getTestRest().getForEntity("/users/auth", Map.class);
        });

        Then("^user info is provided$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map map = response.getBody();
            assertEquals("user", map.get("username"));
            assertEquals(false, map.get("prodUser"));
            assertEquals(false, map.get("techUser"));
        });
    }
}
