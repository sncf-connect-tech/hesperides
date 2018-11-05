package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SearchModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public SearchModules() {

        Given("^a list of( \\d+)? modules( with different names)?(?: with the same name)?$", (String modulesCount, String withDifferentNames) -> {
            Integer modulesToCreateCount = StringUtils.isEmpty(modulesCount) ? 12 : Integer.valueOf(modulesCount.substring(1));
            for (int i = 0; i < modulesToCreateCount; i++) {
                if (StringUtils.isNotEmpty(withDifferentNames)) {
                    moduleBuilder.withName("new-module-" + i);
                } else {
                    moduleBuilder.withName("new-module");
                }
                moduleBuilder.withVersion("0.0." + i + 1);
                moduleClient.create(moduleBuilder.build());
            }
        });

        When("^I search for one specific module$", () -> {
            testContext.responseEntity = moduleClient.search("new-module 0.0.3");
        });

        When("^I search for some of those modules$", () -> {
            testContext.responseEntity = moduleClient.search("new-module");
        });

        When("^I search for a module that does not exist$", () -> {
            testContext.responseEntity = moduleClient.search("nope");
        });

        When("^I try to search for a module with no search terms$", () -> {
            testContext.responseEntity = moduleClient.search("", String.class);
        });

        Then("^the module is found$", () -> {
            assertOK();
            assertEquals(1, getBodyAsArray().length);
        });

        Then("^the list of module results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), getBodyAsArray().length);
        });

        Then("^the search request is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }
}
