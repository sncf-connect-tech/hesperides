package org.hesperides.test.bdd.users;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.UserInfoOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetUserInformation extends HesperidesScenario implements En {

    @Autowired
    private UserClient userClient;

    public GetUserInformation() {

        When("^I get the current user information$", () ->
                userClient.getCurrentUserInfo());

        When("^I get user information about another prod user$", () ->
                userClient.getUserInfo(authorizationCredentialsConfig.getProdUsername()));

        When("^I get user information about a non-existing user$", () ->
                userClient.getUserInfo("inexistant", "should-fail"));

        Then("^(.+) is listed under the user directory groups$", (String expectedAuthorityGroup) -> {
            assertOK();
            List<String> actualAuthorityGroups = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getDirectoryGroupCNs();
            String realDirectoryGroup = authorizationCredentialsConfig.getRealDirectoryGroup(expectedAuthorityGroup);
            assertThat(actualAuthorityGroups, hasItem(realDirectoryGroup));
        });

        Then("^(.+) is listed under the user authority roles$", (String expectedAuthorityRole) -> {
            assertOK();
            List<String> actualAuthorityRoles = testContext.getResponseBody(UserInfoOutput.class).getAuthorities().getRoles();
            assertThat(actualAuthorityRoles, hasItem(expectedAuthorityRole));
        });

        When("^(?:the user log out|the user re-send valid credentials)$", () ->
                userClient.logout());

        Then("^login is successful$", this::assertOK);

        Then("^user information is returned, (with|without) tech role and (with|without) prod role$", (
                String withTechRole, String withProdRole) -> {
            assertOK();
            UserInfoOutput actualUserInfo = testContext.getResponseBody();
            assertEquals("with".equals(withTechRole), actualUserInfo.getTechUser());
            assertEquals("with".equals(withProdRole), actualUserInfo.getProdUser());
        });
    }
}
