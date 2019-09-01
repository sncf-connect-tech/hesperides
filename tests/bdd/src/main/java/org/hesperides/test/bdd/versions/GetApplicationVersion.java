package org.hesperides.test.bdd.versions;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class GetApplicationVersion extends HesperidesScenario implements En {

    public GetApplicationVersion() {

        When("^I get the application versions$", () -> {
            restTemplate.getForEntity("/versions", Map.class);
        });

        Then("^the versions are returned$", () -> {
            assertOK();
            Map map = testContext.getResponseBodyAsMap();
            assertNotNull(map.get("version"));
        });
    }
}
