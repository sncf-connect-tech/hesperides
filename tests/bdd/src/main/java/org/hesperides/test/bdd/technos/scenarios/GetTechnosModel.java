package org.hesperides.test.bdd.technos.scenarios;

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GetTechnosModel extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnosModel() {

        When("^I( try to)? get the model of this techno$", (String tryTo) -> technoClient.getModel(technoBuilder.build(), tryTo));

        Then("^the model of this techno contains the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = technoBuilder.buildPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the techno model is empty$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelOutput(Collections.emptySet(), Collections.emptySet());
            ModelOutput actualModel = testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });
    }
}
