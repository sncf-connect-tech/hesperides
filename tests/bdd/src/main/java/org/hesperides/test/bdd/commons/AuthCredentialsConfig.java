package org.hesperides.test.bdd.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthCredentialsConfig {
    // Ces valeurs sont employées dans les tests.feature BDD :
    public static final String LAMBDA_TEST_PROFILE = "lambda";
    private static final String TECH_TEST_PROFILE = "tech";
    public static final String PROD_TEST_PROFILE = "prod";

    @Setter
    @Getter
    private String lambdaUserName = "user";
    @Setter
    @Getter
    private String lambdaUserPassword = "password";

    @Setter
    @Getter
    private String techUserName = "tech";
    @Setter
    @Getter
    private String techUserPassword = "password";

    @Setter
    @Getter
    private String prodUserName = "prod";
    @Setter
    @Getter
    private String prodUserPassword = "password";

    @Setter
    @Getter
    private String lambdaUserParentGroupDN;

    public BasicAuthenticationInterceptor getBasicAuthInterceptorForTestProfile(String testProfile) {
        return new BasicAuthenticationInterceptor(getUsernameForTestProfile(testProfile),
                getPasswordForTestProfile(testProfile));
    }

    public String getUsernameForTestProfile(String testProfile) {
        String username;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            username = lambdaUserName;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            username = techUserName;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            username = prodUserName;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return username;
    }

    public String getPasswordForTestProfile(String testProfile) {
        String username;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            username = lambdaUserPassword;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            username = techUserPassword;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            username = prodUserPassword;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return username;
    }
}
