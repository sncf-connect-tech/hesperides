package org.hesperides.tests.bddrefacto.users;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetUserInformation implements En {

    @Autowired
    private RestTemplate restTemplate;

    private ResponseEntity<Map> responseEntity;

    public GetUserInformation() {

        When("^I get the current user information$", () -> {
            responseEntity = restTemplate.getForEntity("/users/auth", Map.class);
        });

        Then("^the user information is provided$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Map map = responseEntity.getBody();
            assertEquals("user", map.get("username"));
            assertEquals(false, map.get("prodUser"));
            assertEquals(false, map.get("techUser"));
        });
    }
}
