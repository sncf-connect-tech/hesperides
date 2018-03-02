package org.hesperides.tests.bdd.commons.contexts;

import cucumber.api.java.en.Given;
import org.hesperides.tests.bdd.CucumberSpringBean;

public class UserContext extends CucumberSpringBean {

    @Given("^an authenticated user$")
    public void aAuthenticatedUser() {
        template.addCreds("user", "password");
    }
}
