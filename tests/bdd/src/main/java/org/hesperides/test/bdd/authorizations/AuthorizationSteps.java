/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.authorizations;

import cucumber.api.java8.En;
import org.hesperides.core.infrastructure.security.LdapAuthenticationProvider;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

import java.util.Collections;
import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hesperides.test.bdd.commons.AuthCredentialsConfig.LAMBDA_TEST_PROFILE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AuthorizationSteps extends HesperidesScenario implements En {

    @Autowired(required = false)
    private LdapAuthenticationProvider ldapAuthenticationProvider;

    public AuthorizationSteps() {

        Given("^(?:as )?an? (?:authenticated|known) ?(.*)? user$", this::setAuthUserRole);

        Given("^a user belonging to A_GROUP$", () -> {
            setAuthUserRole(LAMBDA_TEST_PROFILE);
            assertNotNull("Bean not autowired, probably because profile NOLDAP is in use", ldapAuthenticationProvider);
            HashSet<String> userGroupsDNs = ldapAuthenticationProvider.getUserGroupsDN(
                    testContext.getUsername(),
                    testContext.getPassword());
            assertThat(userGroupsDNs, hasItems(authCredentialsConfig.getLambdaUserParentGroupDN()));
        });

        Given("^a user that does not belong to any group$", () -> {

        });
    }

    public void setAuthUserRole(String authorizationRole) {
        // Note: we erase ALL interceptors here by simplicity, because we know only the BasicAuth one is used in this app
        final String testProfile = defaultIfEmpty(authorizationRole, LAMBDA_TEST_PROFILE);
        restTemplate.setInterceptors(Collections.singletonList(authCredentialsConfig.getBasicAuthInterceptorForTestProfile(testProfile)));
        testContext.setAuthorizationRole(authorizationRole);
    }

    public void ensureUserAuthIsSet() {
        if (restTemplate.getInterceptors().stream().noneMatch(i -> i instanceof BasicAuthenticationInterceptor)) {
            setAuthUserRole(null); // => Active le profil par défault (lambda)
        }
    }
}
