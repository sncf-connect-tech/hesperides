package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class SearchModules implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<ModuleIO[]> responseEntity;

    public SearchModules() {

        Given("^a list of (\\d+) modules( with different names)?(?: with the same name)?$",
                (final Integer nbModules, final String withDifferentNames) -> {
            for (int i = 0; i < nbModules; i++) {
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
            responseEntity = moduleClient.search("new-module 0.0.3");
        });

        When("^I search for some of those modules$", () -> {
            responseEntity = moduleClient.search("new-module");
        });

        When("^I search for a module that does not exist$", () -> {
            responseEntity = moduleClient.search("nope");
        });

        Then("^the module is found$", () -> {
            assertOK(responseEntity);
            assertEquals(1, responseEntity.getBody().length);
        });

        Then("^the list of module results is limited to (\\d+) items$", (final Integer limit) -> {
            assertOK(responseEntity);
            assertEquals(limit.intValue(), responseEntity.getBody().length);
        });

        Then("^the list of module results is empty$", () -> {
            assertOK(responseEntity);
            assertEquals(0, responseEntity.getBody().length);
        });
    }
}
