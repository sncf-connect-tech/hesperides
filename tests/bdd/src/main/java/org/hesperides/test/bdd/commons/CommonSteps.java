package org.hesperides.test.bdd.commons;

import cucumber.api.java8.En;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CommonSteps extends HesperidesScenario implements En {

    public CommonSteps() {

        Then("^the request is successful$", this::assertOK);

        Then("^the resource is not found$", this::assertNotFound);

        Then("^the request is rejected with a bad request error$", this::assertBadRequest);

        Then("^the request is rejected with an internal error$", this::assertInternalServerError);

        Then("^the request is rejected with an unauthorized error$", this::assertUnauthorized);

        Then("^the request is rejected with a forbidden error$", this::assertForbidden);

        Then("^an empty list is returned$", () -> {
            assertOK();
            assertEquals(0, testContext.getResponseBodyArrayLength());
        });

        Then("^a list of (\\d+) elements? is returned$", (Integer expectedCount) -> {
            assertOK();
            assertEquals(expectedCount.intValue(), testContext.getResponseBodyArrayLength());
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), testContext.getResponseStatusCode());
            assertThat(testContext.getResponseBody(String.class), containsString(message));
        });
    }
}
