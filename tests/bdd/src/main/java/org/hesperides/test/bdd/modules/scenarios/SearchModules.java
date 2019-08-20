package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class SearchModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public SearchModules() {

        When("^I search for one specific module( using the wrong case)?$", (String usingTheWrongCase) -> {
            String searchInput = "new-module 0.0.13";
            if (isNotEmpty(usingTheWrongCase)) {
                searchInput = searchInput.toUpperCase();
            }
            moduleClient.searchModules(searchInput);
        });

        When("^I search for some of those modules(?:, limiting the number of results to (\\d+))?$", (String nbResults) -> {
            Integer size = StringUtils.isEmpty(nbResults) ? 0 : Integer.parseInt(nbResults);
            moduleClient.searchModules("new-module", size, null);
        });

        When("^I search for a module that does not exist$", () -> moduleClient.searchModules("nope"));

        When("^I try to search for a module with no search input$", () -> {
            moduleClient.searchModules("", 0, "should-fail");
        });

        When("^I search for a single module using only the name and version of this module$", () -> {
            String searchInput = moduleBuilder.getName() + " " + moduleBuilder.getVersion();
            moduleClient.searchSingle(searchInput);
        });

        When("^I search for the (released|working copy)? version of this module$", (String releasedOrWorkingCopy) -> {
            String searchInput = moduleBuilder.getName() + " " + moduleBuilder.getVersion() + " ";
            searchInput += "released".equals(releasedOrWorkingCopy) ? "false" : "true";
            moduleClient.searchSingle(searchInput);
        });

        When("^I search for modules, using an existing module name, version and version type$", () -> {
            moduleClient.searchModules("new-module 0.0.1 true");
        });

        When("^I search for modules, using an existing module name and version$", () -> {
            moduleClient.searchModules("new-module 0.0.1");
        });

        When("^I search for modules, using an existing module name$", () -> {
            moduleClient.searchModules("new-module");
        });

        Then("^the module is found$", () -> {
            assertOK();
            assertEquals(1, testContext.getResponseBodyArrayLength());
        });

        Then("^the list of module results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), testContext.getResponseBodyArrayLength());
        });

        Then("^the search request is rejected with a bad request error$", this::assertBadRequest);

        Then("^I get the working copy version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleHistory.getFirstModuleBuilder().build();
            ModuleIO actualModule = testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^I get the released version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the first module in the results is this module$", () -> {
            assertOK();
            ModuleIO[] returnedModules = testContext.getResponseBodyAsArray();
            assertEquals(returnedModules[0].toDomainInstance().getKey().getNamespaceWithoutPrefix(), "new-module#0.0.1#WORKINGCOPY");
        });

        Then("^the first module in the results has exactly this name$", () -> {
            assertOK();
            ModuleIO[] returnedModules = testContext.getResponseBodyAsArray();
            assertEquals(returnedModules[0].toDomainInstance().getKey().getName(), "new-module");
        });

        Then("^the first module in the results has exactly this name and version$", () -> {
            assertOK();
            ModuleIO[] returnedModules = testContext.getResponseBodyAsArray();
            assertEquals(returnedModules[0].toDomainInstance().getKey().getName(), "new-module");
            assertEquals(returnedModules[0].toDomainInstance().getKey().getVersion(), "0.0.1");
        });
    }
}
