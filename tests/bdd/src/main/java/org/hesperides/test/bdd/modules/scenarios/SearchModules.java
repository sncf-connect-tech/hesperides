package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.greaterThan;
import static org.hesperides.test.bdd.versions.MatcherUtils.semVerGreaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SearchModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public SearchModules() {

        When("^I search for one specific module( using the wrong case)?$", (String wrongCase) -> {
            String input = "new-module 0.0.3";
            if (StringUtils.isNotEmpty(wrongCase)) {
                input = input.toUpperCase();
            }
            testContext.responseEntity = moduleClient.search(input);
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

        When("^I search for a single module using only the name and version of this module$", () -> {
            testContext.responseEntity = moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion(), ModuleIO.class);
        });

        When("^I search for the released version of this single module$", () -> {
            testContext.responseEntity = moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion() + " false", ModuleIO.class);
        });

        When("^I search for the working copy version of this single module$", () -> {
            testContext.responseEntity = moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion() + " true", ModuleIO.class);
        });

        Then("^the module is found$", () -> {
            assertOK();
            assertEquals(1, getBodyAsArray().length);
        });

        Then("^the list of module results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), getBodyAsArray().length);
        });

        Then("^the resulting list is ordered using semantic versioning starting by the highest ones$", () -> {
            assertOK();
            ModuleIO[] modulesFound = getBodyAsArray();
            for (int i = 0; i < modulesFound.length - 1; i++) {
                assertThat(modulesFound[i].getVersion(), semVerGreaterThan(modulesFound[i + 1].getVersion()));
            }
        });

        Then("^the search request is rejected with a bad request error$", () -> {
            assertBadRequest();
        });

        Then("^I get the working copy version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionType(ModuleIO.WORKINGCOPY).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^I get the released version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionType(ModuleIO.RELEASE).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });
    }
}
