package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GetTechnosModel extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public GetTechnosModel() {

        When("^I( try to)? get the model of this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.getModel(technoBuilder.build(), getResponseType(tryTo, ModelOutput.class)));
        });

        Then("^the model of this techno contains the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = technoBuilder.buildPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the techno model is empty$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelOutput(Collections.emptySet(), Collections.emptySet());
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the model of this techno doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });
    }
}
