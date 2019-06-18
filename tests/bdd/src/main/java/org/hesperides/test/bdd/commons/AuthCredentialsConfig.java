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
    private static final String PROD_TEST_PROFILE = "prod";

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
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            return lambdaUserName;
        }
        if (TECH_TEST_PROFILE.equals(testProfile)) {
            return techUserName;
        }
        if (PROD_TEST_PROFILE.equals(testProfile)) {
            return prodUserName;
        }
        throw new IllegalArgumentException("Unknown test profile: " + testProfile);
    }

    public String getPasswordForTestProfile(String testProfile) {
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            return lambdaUserPassword;
        }
        if (TECH_TEST_PROFILE.equals(testProfile)) {
            return techUserPassword;
        }
        if (PROD_TEST_PROFILE.equals(testProfile)) {
            return prodUserPassword;
        }
        throw new IllegalArgumentException("Unknown test profile: " + testProfile);
    }
}
