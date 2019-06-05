package org.hesperides.test.bdd.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue") // Create and destroy bean on each scenario test
public class TestContext {
    @Autowired
    public
    AuthCredentialsConfig authCredentialsConfig;

    String authRole = null;
    String getUsername() {
        return authCredentialsConfig.getUsernameForTestProfile(authRole);
    }
    String getPassword() {
        return authCredentialsConfig.getPasswordForTestProfile(authRole);
    }

    public ResponseEntity responseEntity;

    public Object getResponseBody() {
        return responseEntity.getBody();
    }
}
