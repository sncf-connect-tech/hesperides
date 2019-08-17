package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class GetModulesModel extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModulesModel() {

        When("^I( try to)? get the model of this module$", (String tryTo) -> moduleClient.getModel(moduleBuilder.build(), tryTo));

        Then("^the model of this module contains the(?: updated)? properties$", () -> {
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

        Then("^the module model is not found$", this::assertNotFound);

        Then("^the model of this module doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the model of this module has (\\d+) simple propert(?:y|ies)$", (Integer nbSimpleProperties) -> {
            assertOK();
            Set<PropertyOutput> actualSimpleProperties = testContext.getResponseBody(ModelOutput.class).getProperties();
            assertThat(actualSimpleProperties, hasSize(nbSimpleProperties));
        });

        Then("^the model of property \"([^\"]+)\"" +
                "( is a required password)?" +
                "(?: has a comment of \"([^\"]+)\")?" +
                "(?: and(?: has)? a default value of \"([^\"]+)\")?", (
                String propertyName,
                String isRequiredPassword,
                String comment,
                String defaultValue) -> {

            assertOK();

            Set<PropertyOutput> actualProperties = testContext.getResponseBody(ModelOutput.class).getProperties();
            PropertyOutput matchingPropertyModel = actualProperties.stream()
                    .filter(property -> property.getName().equals(propertyName))
                    .findAny()
                    .orElse(null);
            assertNotNull(matchingPropertyModel);
            if (isNotBlank(isRequiredPassword)) {
                assertTrue(matchingPropertyModel.isRequired());
                assertTrue(matchingPropertyModel.isPassword());
            }
            if (isNotBlank(comment)) {
                assertEquals(comment, matchingPropertyModel.getComment());
            }
            if (isNotBlank(defaultValue)) {
                assertEquals(matchingPropertyModel.getDefaultValue(), defaultValue);
            }
        });
    }
}
