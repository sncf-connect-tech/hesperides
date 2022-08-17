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
package org.hesperides.test.bdd.users;

import io.cucumber.java8.En;
import org.assertj.core.api.Assertions;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.infrastructure.security.LdapAuthenticationProvider;
import org.hesperides.core.presentation.io.UserInfoOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.configuration.AuthorizationCredentialsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hesperides.test.bdd.configuration.AuthorizationCredentialsConfig.LAMBDA_TEST_PROFILE;
import static org.hesperides.test.bdd.configuration.AuthorizationCredentialsConfig.NOGROUP_TEST_PROFILE;
import static org.junit.Assert.assertNotNull;

public class UserAuthorities extends HesperidesScenario implements En {

    @Autowired(required = false)
    private LdapAuthenticationProvider ldapAuthenticationProvider;
    @Autowired
    private AuthorizationCredentialsConfig authorizationCredentialsConfig;

    public UserAuthorities() {

        Given("^(?:as )?an? (?:authenticated|known) ?(.*)? user$", this::setAuthUserRole);

        Given("^a (.*)? user belonging to the directory group (.*)?", (String role, String directoryGroup) -> {
            Set<String> userGroupCNs = setRoleAndReturnUserGroupCNs(role);
            assertThat(userGroupCNs, hasItems(authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup)));
        });

        Given("^a (.*)? user not belonging to the directory group (.*)?", (String role, String directoryGroup) -> {
            Set<String> userGroupCNs = setRoleAndReturnUserGroupCNs(role);
            assertThat(userGroupCNs, not(hasItem(authorizationCredentialsConfig.getRealDirectoryGroup(directoryGroup))));
        });

        Given("^a user that does not belong to any group$", () -> {
            Set<String> userGroupCNs = setRoleAndReturnUserGroupCNs(NOGROUP_TEST_PROFILE);
            Assertions.assertThat(userGroupCNs).isEmpty();
        });

        Then("^the user is retrieved without any group$", () -> {
            assertOK();
            final List<String> directoryGroupCNs = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getDirectoryGroupCNs();
            Assertions.assertThat(directoryGroupCNs).isEmpty();
        });
    }

    private Set<String> setRoleAndReturnUserGroupCNs(String role) {
        setAuthUserRole(role);
        assertNotNull("Bean not autowired, probably because profile NOLDAP is in use", ldapAuthenticationProvider);
        HashSet<String> userGroupsDNs = ldapAuthenticationProvider.getUserGroupsDN(testContext.getUsername(), testContext.getPassword());
        return userGroupsDNs.stream().map(DirectoryGroupDN::extractCnFromDn).collect(Collectors.toSet());
    }

    public void setAuthUserRole(String authorizationRole) {
        // Note: we erase ALL interceptors here by simplicity, because we know only the BasicAuth one is used in this app
        final String testProfile = defaultIfEmpty(authorizationRole, LAMBDA_TEST_PROFILE);
        BasicAuthenticationInterceptor authInterceptor = authorizationCredentialsConfig.getBasicAuthInterceptorForTestProfile(testProfile);
        restTemplate.setInterceptors(Collections.singletonList(authInterceptor));
        testContext.setAuthorizationRole(authorizationRole);
    }

    public void ensureUserAuthIsSet() {
        if (restTemplate.getInterceptors().stream().noneMatch(i -> i instanceof BasicAuthenticationInterceptor)) {
            setAuthUserRole(null); // => Active le profil par défault (lambda)
        }
    }
}
