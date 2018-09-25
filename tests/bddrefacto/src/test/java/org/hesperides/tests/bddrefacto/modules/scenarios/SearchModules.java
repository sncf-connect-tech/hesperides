package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class SearchModules implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity<ModuleIO[]> responseEntity;

    public SearchModules() {

        Given("^a list of (\\d+) modules$", (final Integer nbModules) -> {
            for (int i = 0; i < nbModules; i++) {
                moduleBuilder.withName("a-module").withVersion("0.0." + i + 1);
                moduleClient.create(moduleBuilder.build(), ModuleIO.class);
            }
        });

        When("^I search for one specific module$", () -> {
            responseEntity = moduleClient.search("a-module 0.0.3");
        });

        When("^I search for some of those modules$", () -> {
            responseEntity = moduleClient.search("a-module");
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
