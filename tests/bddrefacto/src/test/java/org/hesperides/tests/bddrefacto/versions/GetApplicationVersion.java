package org.hesperides.tests.bddrefacto.versions;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertNotNull;

public class GetApplicationVersion implements En {

    @Autowired
    private RestTemplate restTemplate;

    private ResponseEntity<Map> responseEntity;

    public GetApplicationVersion() {

        When("^I get the application versions$", () -> {
            responseEntity = restTemplate.getForEntity("/versions", Map.class);
        });

        Then("^the versions are returned$", () -> {
            assertOK(responseEntity);
            Map map = responseEntity.getBody();
            assertNotNull(map.get("backend_version"));
            assertNotNull(map.get("api_version"));
        });
    }
}
