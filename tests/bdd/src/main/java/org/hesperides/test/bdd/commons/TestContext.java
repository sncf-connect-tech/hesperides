package org.hesperides.test.bdd.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue") // Create and destroy bean on each scenario test
public class TestContext {

    private String authorizationRole = null;
    private ResponseEntity responseEntity;

    @Autowired
    private AuthorizationCredentialsConfig authorizationCredentialsConfig;


    public String getUsername() {
        return authorizationCredentialsConfig.getTestProfileUsername(authorizationRole);
    }

    public String getPassword() {
        return authorizationCredentialsConfig.getTestProfilePassword(authorizationRole);
    }

    public <R> R getResponseBody(Class<R> responseType) {
        return responseType.cast(responseEntity.getBody());
    }

    public void setAuthorizationRole(String authorizationRole) {
        this.authorizationRole = authorizationRole;
    }

    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }

    public HttpStatus getResponseStatusCode() {
        return responseEntity.getStatusCode();
    }
}
