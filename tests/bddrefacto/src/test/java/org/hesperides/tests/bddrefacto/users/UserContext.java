package org.hesperides.tests.bddrefacto.users;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

public class UserContext implements En {

    @Autowired
    private RestTemplate restTemplate;

    public UserContext() {
        Given("^an authenticated user$", () -> {
            restTemplate.getInterceptors().add(
                    new BasicAuthorizationInterceptor("user", "password"));
        });
    }
}
