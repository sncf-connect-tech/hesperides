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
    private String lambdaUsername = "user";
    @Setter
    private String lambdaPassword = "password";

    @Setter
    private String techUsername = "tech";
    @Setter
    private String techPassword = "password";

    @Setter
    @Getter
    private String prodUsername = "prod";
    @Setter
    private String prodPassword = "password";

    @Setter
    @Getter
    private String lambdaParentGroupDN;

    public BasicAuthenticationInterceptor getBasicAuthInterceptorForTestProfile(String testProfile) {
        return new BasicAuthenticationInterceptor(getTestProfileUsername(testProfile),
                getTestProfilePassword(testProfile));
    }

    public String getTestProfileUsername(String testProfile) {
        String username;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            username = lambdaUsername;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            username = techUsername;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            username = prodUsername;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return username;
    }

    public String getTestProfilePassword(String testProfile) {
        String password;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            password = lambdaPassword;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            password = techPassword;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            password = prodPassword;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return password;
    }
}
