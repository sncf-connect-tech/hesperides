package org.hesperides.test.bdd.commons;

import cucumber.api.java8.En;
import org.hesperides.core.infrastructure.security.LdapAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hesperides.test.bdd.commons.AuthCredentialsConfig.LAMBDA_TEST_PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CommonSteps extends HesperidesScenario implements En {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TestContext testContext;
    @Autowired(required = false)
    private LdapAuthenticationProvider ldapAuthenticationProvider;

    public CommonSteps() {
        Given("^(?:as )?an? (?:authenticated|known) ?(.*)? user$", this::setAuthUserRole);

        Given("^a user belonging to A_GROUP$", () -> {
            setAuthUserRole(LAMBDA_TEST_PROFILE);
            assertNotNull("Bean not autowired, probably because profile NOLDAP is in use", ldapAuthenticationProvider);
            HashSet<String> userGroupsDNs = ldapAuthenticationProvider.getUserGroupsDN(
                    testContext.getUsername(),
                    testContext.getPassword());
            assertThat(userGroupsDNs, hasItems(testContext.authCredentialsConfig.getLambdaUserParentGroupDN()));
        });

        Then("^the resource is not found$", this::assertNotFound);

        Then("^the request is rejected with a bad request error$", this::assertBadRequest);

        Then("^the request is rejected with an internal error$", this::assertInternalServerError);

        Then("^the request is rejected with an unauthorized error$", this::assertUnauthorized);

        Then("^an empty list is returned$", () -> {
            assertOK();
            assertEquals(0, getBodyAsArray().length);
        });

        Then("^a list of (\\d+) elements? is returned$", (Integer expectedCount) -> {
            assertOK();
            assertEquals(expectedCount.intValue(), getBodyAsArray().length);
        });
    }

    public void setAuthUserRole(String authRole) {
        // Note: we erase ALL interceptors here by simplicity, because we know only the BasicAuth one is used in this app
        restTemplate.setInterceptors(Collections.singletonList(testContext.authCredentialsConfig.getBasicAuthInterceptorForTestProfile(defaultIfEmpty(authRole, LAMBDA_TEST_PROFILE))));
        testContext.authRole = authRole;
    }

    public void ensureUserAuthIsSet() {
        if (!restTemplate.getInterceptors().stream().anyMatch(i -> i instanceof BasicAuthenticationInterceptor)) {
            setAuthUserRole(null);
        }
    }
}
