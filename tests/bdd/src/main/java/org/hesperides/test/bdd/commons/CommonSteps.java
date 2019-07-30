package org.hesperides.test.bdd.commons;

import cucumber.api.java8.En;

import static org.junit.Assert.assertEquals;

public class CommonSteps extends HesperidesScenario implements En {

    public CommonSteps() {

        Then("^the resource is not found$", this::assertNotFound);

        Then("^the request is rejected with a bad request error$", this::assertBadRequest);

        Then("^the request is rejected with an internal error$", this::assertInternalServerError);

        Then("^the request is rejected with an unauthorized error$", this::assertUnauthorized);

        Then("^the request is rejected with a forbidden error$", this::assertForbidden);

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
