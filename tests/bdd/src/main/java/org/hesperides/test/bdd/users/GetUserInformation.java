package org.hesperides.test.bdd.users;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.UserInfoOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetUserInformation extends HesperidesScenario implements En {

    public GetUserInformation() {

        When("^I get the current user information$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/auth", UserInfoOutput.class));
        });

        When("^I get user information about another prod user$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/" + authorizationCredentialsConfig.getProdUsername(), UserInfoOutput.class));
        });

        When("^I get user information about a non-existing user$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/inexistant", String.class));
        });

        Then("^(.+) is listed under the user directory groups$", (String expectedAuthorityGroup) -> {
            assertOK();
            List<String> actualAuthorityGroups = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getDirectoryGroupCNs();
            final String realDirectoryGroup = authorizationCredentialsConfig.getRealDirectoryGroup(expectedAuthorityGroup);
            assertThat(actualAuthorityGroups, hasItem(realDirectoryGroup));
        });

        Then("^(.+) is listed under the user authority roles$", (String expectedAuthorityRole) -> {
            assertOK();
            List<String> actualAuthorityRoles = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getRoles();
            assertThat(actualAuthorityRoles, hasItem(expectedAuthorityRole));
        });

        When("^(?:the user log out|the user re-send valid credentials)$", () ->
                testContext.setResponseEntity(restTemplate.getForEntity("/users/auth?logout=true", String.class))
        );

        Then("^login is successful$", () ->
                assertOK()
        );

        Then("^user information is returned, (with|without) tech role and (with|without) prod role$", (
                String withTechRole, String withProdRole) -> {
            assertOK();
            final UserInfoOutput actualUserInfo = testContext.getResponseBody(UserInfoOutput.class);
            assertEquals("with".equals(withTechRole), actualUserInfo.getTechUser());
            assertEquals("with".equals(withProdRole), actualUserInfo.getProdUser());
        });
    }
}
