package org.hesperides.test.bdd.commons;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

public class CommonSteps extends HesperidesScenario implements En {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TestContext testContext;

    public CommonSteps() {
        Given("^an authenticated user$", () -> {
            restTemplate.getInterceptors().add(testContext.getBasicAuthInterceptor());
        });

        Then("^the resource is not found$", () -> {
            assertNotFound();
        });

        Then("^the request is rejected with a bad request error$", () -> {
            assertBadRequest();
        });

        Then("^an empty list is returned$", () -> {
            assertOK();
            assertEquals(0, getBodyAsArray().length);
        });

        Then("^a list of (\\d+) elements? is returned$", (Integer expectedCount) -> {
            assertOK();
            assertEquals(expectedCount.intValue(), getBodyAsArray().length);
        });
    }
}
