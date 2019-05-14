package org.hesperides.test.bdd.commons;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.junit.Assert.assertEquals;

public class CommonSteps extends HesperidesScenario implements En {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TestContext testContext;

    public CommonSteps() {
        Given("^(?:as )?an? (?:authenticated|known) ?(.*)? user$", this::setAuthUserRole);

        Then("^the resource is not found$", this::assertNotFound);

        Then("^the request is rejected with a bad request error$", this::assertBadRequest);

        Then("^the request is rejected with an internal error$", this::assertInternalServerError);

        Then("^an empty list is returned$", () -> {
            assertOK();
            assertEquals(0, getBodyAsArray().length);
        });

        Then("^a list of (\\d+) elements? is returned$", (Integer expectedCount) -> {
            assertOK();
            assertEquals(expectedCount.intValue(), getBodyAsArray().length);
        });
    }

    public void setAuthUserRole(String authRole) {
        // Note: we erase ALL interceptors here by simplicity, because we know only the BasicAuth one is used in this app
        restTemplate.setInterceptors(Collections.singletonList(testContext.getBasicAuthInterceptorForUser(defaultIfEmpty(authRole, "lambda"))));
    }

    public void ensureUserAuthIsSet() {
        if (!restTemplate.getInterceptors().stream().anyMatch(i -> i instanceof BasicAuthenticationInterceptor)) {
            setAuthUserRole(null);
        }
    }
}
