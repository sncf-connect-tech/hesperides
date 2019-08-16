package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SearchModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public SearchModules() {

        When("^I search for one specific module$", () -> moduleClient.searchModules("test-module 0.3"));

        When("^I search for some of those modules(?:, limiting the number of results to (\\d+))?$", (String nbResults) -> {
            Integer size = StringUtils.isEmpty(nbResults) ? 0 : Integer.parseInt(nbResults);
            moduleClient.searchModules("test-module", size);
        });

        When("^I search for a module that does not exist$", () -> moduleClient.searchModules("nope"));

        Then("^the module is found$", () -> {
            assertOK();
            assertEquals(1, testContext.getResponseBodyArrayLength());
        });

        Then("^the list of module results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), testContext.getResponseBodyArrayLength());
        });
    }
}
