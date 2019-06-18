package org.hesperides.test.bdd.users;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.commons.TestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.extractCN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetUserInformation extends HesperidesScenario implements En {

    @Autowired
    private TestContext testContext;
    @Autowired
    private RestTemplate restTemplate;

    public GetUserInformation() {

        When("^I get the current user information$", () -> {
            testContext.setResponseEntity(restTemplate.getForEntity("/users/auth", Map.class));
        });

        Then("^(.+) is listed in the user authorities$", (String authority) -> {
            assertEquals(HttpStatus.OK, testContext.getResponseStatusCode());
            List<String> authorities = extractAuthoritiesValues((List<Map<String, String>>)getBodyAsMap().get("authorities"));
            if (authority.equals("A_GROUP")) {
                authority = extractCN(authCredentialsConfig.getLambdaUserParentGroupDN());
            }
            assertThat(authorities, hasItems(authority));
        });

        When("^(?:the user log out|the user re-send valid credentials)$", () ->
                testContext.setResponseEntity(restTemplate.getForEntity("/users/auth?logout=true", String.class))
        );

        Then("^login is successful$", () ->
                assertEquals(HttpStatus.OK, testContext.getResponseStatusCode())
        );

        Then("^user information is returned, (with|without) tech role and (with|without) prod role$",
                (String withTechRole, String withProdRole) -> {
                    assertEquals(HttpStatus.OK, testContext.getResponseStatusCode());
                    Map body = getBodyAsMap();
                    assertEquals("with".equals(withTechRole), body.get("techUser"));
                    assertEquals("with".equals(withProdRole), body.get("prodUser"));
                });
    }

    public static List<String> extractAuthoritiesValues(List<Map<String, String>> authorities) {
        return authorities.stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
