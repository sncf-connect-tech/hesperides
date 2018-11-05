package org.hesperides.tests.bdd.commons;

import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue") // Create and destroy bean on each scenario test
public class TestContext {
    public static BasicAuthorizationInterceptor BASIC_AUTH_INTERCEPTOR = new BasicAuthorizationInterceptor("user", "password");

    public ResponseEntity responseEntity;
}
