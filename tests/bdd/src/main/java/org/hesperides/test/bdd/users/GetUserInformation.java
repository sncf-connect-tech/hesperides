package org.hesperides.test.bdd.users;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.UserInfoOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.extractCN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetUserInformation extends HesperidesScenario implements En {

    public GetUserInformation() {

        When("^I get the current user information$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/auth", UserInfoOutput.class));
        });

        When("^I get user information about another prod user$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/" + authCredentialsConfig.getProdUsername(), UserInfoOutput.class));
        });

        When("^I get user information about a non-existing user$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/inexistant", String.class));
        });

        Then("^the given group is listed under the user directory groups$", () -> {
            assertEquals(HttpStatus.OK, testContext.getResponseStatusCode());
            String expectedAuthorityGroup = extractCN(authCredentialsConfig.getLambdaParentGroupDN());
            List<String> actualAuthorityGroups = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getDirectoryGroups();
            assertThat(actualAuthorityGroups, hasItem(expectedAuthorityGroup));
        });

        Then("^(.+) is listed under the user authority roles$", (String expectedAuthorityRole) -> {
            assertEquals(HttpStatus.OK, testContext.getResponseStatusCode());
            List<String> actualAuthorityRoles = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getRoles();
            assertThat(actualAuthorityRoles, hasItem(expectedAuthorityRole));
        });

        When("^(?:the user log out|the user re-send valid credentials)$", () ->
                testContext.setResponseEntity(restTemplate.getForEntity("/users/auth?logout=true", String.class))
        );

        Then("^login is successful$", () ->
                assertEquals(HttpStatus.OK, testContext.getResponseStatusCode())
        );

        Then("^user information is returned, (with|without) tech role and (with|without) prod role$", (
                String withTechRole, String withProdRole) -> {
            assertEquals(HttpStatus.OK, testContext.getResponseStatusCode());
            final UserInfoOutput actualUserInfo = testContext.getResponseBody(UserInfoOutput.class);
            assertEquals("with".equals(withTechRole), actualUserInfo.getTechUser());
            assertEquals("with".equals(withProdRole), actualUserInfo.getProdUser());
        });
    }

    public static List<String> extractDirectoryGroupsValues(List<Map<String, String>> directoryGroups) {
        return directoryGroups.stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
