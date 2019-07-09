package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.TemplateContainerHelper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SearchModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public SearchModules() {

        When("^I search for one specific module( using the wrong case)?$", (String wrongCase) -> {
            String input = "new-module 0.0.13";
            if (StringUtils.isNotEmpty(wrongCase)) {
                input = input.toUpperCase();
            }
            testContext.setResponseEntity(moduleClient.search(input));
        });

        When("^I search for some of those modules(?:, limiting the number of results to (\\d+))?$", (String nbResults) -> {
            Integer size = StringUtils.isEmpty(nbResults) ? 0 : Integer.valueOf(nbResults);
            testContext.setResponseEntity(moduleClient.search("new-module", size));
        });

        When("^I search for a module that does not exist$", () -> {
            testContext.setResponseEntity(moduleClient.search("nope"));
        });

        When("^I try to search for a module with no search terms$", () -> {
            testContext.setResponseEntity(moduleClient.search("", 0, String.class));
        });

        When("^I search for modules, using an existing module name and version?$", () -> {
            testContext.setResponseEntity(moduleClient.search("new-module 0.0.1 true"));
        });

        When("^I search for a single module using only the name and version of this module$", () -> {
            testContext.setResponseEntity(moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion(), ModuleIO.class));
        });

        When("^I search for the released version of this single module$", () -> {
            testContext.setResponseEntity(moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion() + " false", ModuleIO.class));
        });

        When("^I search for the working copy version of this single module$", () -> {
            testContext.setResponseEntity(moduleClient.singleSearch(moduleBuilder.getName() + " " + moduleBuilder.getVersion() + " true", ModuleIO.class));
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

        Then("^I get the working copy version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionType(TemplateContainerHelper.WORKINGCOPY).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^I get the released version of this module$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionType(TemplateContainerHelper.RELEASE).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the first module in the results is this module$", () -> {
            assertOK();
            ModuleIO[] returnedModules = getBodyAsArray();
            assertEquals(returnedModules[0].toDomainInstance().getKey().getNamespaceWithoutPrefix(), "new-module#0.0.1#WORKINGCOPY");
        });
    }
}
