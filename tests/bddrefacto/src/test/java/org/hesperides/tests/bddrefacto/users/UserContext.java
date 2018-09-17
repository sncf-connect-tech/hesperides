package org.hesperides.tests.bddrefacto.users;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;

public class UserContext implements En {

    @Autowired
    TestRestTemplate testRestTemplate;

    public UserContext() {
        Given("^an authenticated user$", () -> {
            testRestTemplate.getRestTemplate().getInterceptors().add(
                    new BasicAuthorizationInterceptor("user", "password"));
        });
    }
}
