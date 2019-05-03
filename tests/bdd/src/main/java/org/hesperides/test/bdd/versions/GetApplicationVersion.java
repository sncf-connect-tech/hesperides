package org.hesperides.test.bdd.versions;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class GetApplicationVersion extends HesperidesScenario implements En {

    @Autowired
    private RestTemplate restTemplate;

    public GetApplicationVersion() {

        When("^I get the application versions$", () -> {
            testContext.responseEntity = restTemplate.getForEntity("/versions", Map.class);
        });

        Then("^the versions are returned$", () -> {
            assertOK();
            Map map = getBodyAsMap();
            assertNotNull(map.get("build_time"));
            assertNotNull(map.get("version"));
        });
    }
}
