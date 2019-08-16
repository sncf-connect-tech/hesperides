package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GetModulesModel extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModulesModel() {

        When("^I( try to)? get the model of this module$", (String tryTo) -> moduleClient.getModel(moduleBuilder.build(), tryTo));

        Then("^the model of this module contains the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = moduleBuilder.buildPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the module model is empty$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelOutput(Collections.emptySet(), Collections.emptySet());
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the model of this module doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });
    }
}
