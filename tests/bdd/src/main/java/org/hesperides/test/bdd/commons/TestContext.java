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
    private String lambdaUserName = "user";
    @Setter
    private String lambdaUserPassword = "password";

    @Setter
    @Getter
    private String techUserName = "tech";
    @Setter
    private String techUserPassword = "password";

    @Setter
    @Getter
    private String prodUserName = "prod";
    @Setter
    private String prodUserPassword = "password";

    public BasicAuthenticationInterceptor getBasicAuthInterceptorForUser(String username) {
        if ("lambda".equals(username)) {
            return new BasicAuthenticationInterceptor(lambdaUserName, lambdaUserPassword);
        }
        if ("tech".equals(username)) {
            return new BasicAuthenticationInterceptor(techUserName, techUserPassword);
        }
        if ("prod".equals(username)) {
            return new BasicAuthenticationInterceptor(prodUserName, prodUserPassword);
        }
        throw new IllegalArgumentException("Unknown user: " + username);
    }

    public ResponseEntity responseEntity;

    public Object getResponseBody() {
        return responseEntity.getBody();
    }
}
