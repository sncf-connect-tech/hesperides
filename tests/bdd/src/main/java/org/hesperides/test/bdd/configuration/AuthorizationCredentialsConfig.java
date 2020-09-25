package org.hesperides.test.bdd.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthorizationCredentialsConfig {
    // Ces valeurs sont employées dans les tests.feature BDD :
    public static final String LAMBDA_TEST_PROFILE = "lambda";
    public static final String PROD_TEST_PROFILE = "prod";
    public static final String TECH_TEST_PROFILE = "tech";
    public static final String NOGROUP_TEST_PROFILE = "nogroup";

    private static final String A_GROUP = "A_GROUP";
    private static final String ANOTHER_GROUP = "ANOTHER_GROUP";

    @Setter
    private String lambdaUsername = "user";
    @Setter
    private String lambdaPassword = "password";

    @Setter
    @Getter
    private String prodUsername = "prod";
    @Setter
    private String prodPassword = "password";

    @Setter
    private String techUsername = "tech";
    @Setter
    private String techPassword = "password";

    @Setter
    private String nogroupUsername = "nogroup";
    @Setter
    private String nogroupPassword = "password";

    @Setter
    private String prodGroupCN;
    @Setter
    private String otherGroupCN;

    public BasicAuthenticationInterceptor getBasicAuthInterceptorForTestProfile(String testProfile) {
        return new BasicAuthenticationInterceptor(getTestProfileUsername(testProfile),
                getTestProfilePassword(testProfile));
    }

    public String getTestProfileUsername(String testProfile) {
        String username;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            username = lambdaUsername;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            username = prodUsername;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            username = techUsername;
        } else if (NOGROUP_TEST_PROFILE.equals(testProfile)) {
            username = nogroupUsername;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return username;
    }

    public String getTestProfilePassword(String testProfile) {
        String password;
        if (LAMBDA_TEST_PROFILE.equals(testProfile)) {
            password = lambdaPassword;
        } else if (PROD_TEST_PROFILE.equals(testProfile)) {
            password = prodPassword;
        } else if (TECH_TEST_PROFILE.equals(testProfile)) {
            password = techPassword;
        } else if (NOGROUP_TEST_PROFILE.equals(testProfile)) {
            password = nogroupPassword;
        } else {
            throw new IllegalArgumentException("Unknown test profile: " + testProfile);
        }
        return password;
    }

    public String getRealDirectoryGroup(String directoryGroup) {
        String realDirectoryGroup;
        if (ANOTHER_GROUP.equalsIgnoreCase(directoryGroup)) {
            realDirectoryGroup = prodGroupCN;
        } else if (A_GROUP.equalsIgnoreCase(directoryGroup)) {
            realDirectoryGroup = otherGroupCN;
        } else {
            throw new IllegalArgumentException("Unknown directory group: " + directoryGroup);
        }
        return realDirectoryGroup;
    }
}
