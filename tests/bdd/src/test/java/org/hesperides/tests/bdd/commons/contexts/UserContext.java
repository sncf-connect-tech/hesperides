package org.hesperides.tests.bdd.commons.contexts;

import cucumber.api.java.en.Given;
import org.hesperides.tests.bdd.SpringIntegrationTest;

public class UserContext extends SpringIntegrationTest {

    @Given("^an authenticated user$")
    public void aAuthenticatedUser() {
        template.addCreds("user", "password");
    }
}
