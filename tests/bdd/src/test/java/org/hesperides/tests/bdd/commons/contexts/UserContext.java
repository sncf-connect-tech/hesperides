package org.hesperides.tests.bdd.commons.contexts;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;

public class UserContext extends CucumberSpringBean implements En {

    public UserContext() {
        Given("^an authenticated user$", () -> {
            rest.addCreds("user", "password");
        });
    }
}
