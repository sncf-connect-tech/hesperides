package org.hesperides.tests.bdd.commons.contexts;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class UserContext implements En {

    @Autowired
    HesperidesTestRestTemplate rest;

    public UserContext() {
        Given("^an authenticated user$", () -> {
            rest.addCreds("user", "password");
        });
    }
}
