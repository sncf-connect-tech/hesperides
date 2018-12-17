package org.hesperides.test.bdd.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue") // Create and destroy bean on each scenario test
@ConfigurationProperties("auth")
public class TestContext {
    @Setter
    @Getter
    private String username = "tech";
    @Setter
    private String password = "password";

    public BasicAuthenticationInterceptor getBasicAuthInterceptor() {
        return new BasicAuthenticationInterceptor(username, password);
    }

    public ResponseEntity responseEntity;

    public Object getResponseBody() {
        return responseEntity.getBody();
    }
}
