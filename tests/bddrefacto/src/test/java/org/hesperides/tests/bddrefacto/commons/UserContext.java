package org.hesperides.tests.bddrefacto.commons;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

public class UserContext implements En {

    public static final BasicAuthorizationInterceptor BASIC_AUTH_INTERCEPTOR = new BasicAuthorizationInterceptor("user", "password");

    @Autowired
    private RestTemplate restTemplate;

    public UserContext() {
        Given("^an authenticated user$", () -> {
            restTemplate.getInterceptors().add(BASIC_AUTH_INTERCEPTOR);
        });
    }
}
