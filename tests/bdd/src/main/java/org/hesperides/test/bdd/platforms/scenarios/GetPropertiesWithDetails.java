package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesWithDetailsOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertyWithDetailsOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.junit.Assert.assertEquals;

public class GetPropertiesWithDetails extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;

    public GetPropertiesWithDetails() {

        When("^I get the detailed properties of this (platform|module)?$", (String platformOrModule) -> {

            String propertiesPath;
            switch (platformOrModule) {
                case "platform":
                    propertiesPath = "#";
                    break;
                case "module":
                    propertiesPath = deployedModuleBuilder.buildPropertiesPath();
                    break;
                default:
                    throw new RuntimeException("You must choose between platform and module properties");
            }

            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), propertiesPath);
        });

        Then("^the properties details are successfully retrieved and they are empty$", () -> {
            PropertiesWithDetailsOutput expectedProperties = new PropertiesWithDetailsOutput(Collections.emptyList());
            assertPropertiesDetails(expectedProperties);
        });

        Then("^the properties details match these values$", (DataTable data) -> {
            List<TestPropertyWithDetails> testPropertiesWithDetails = data.asList(TestPropertyWithDetails.class);
            PropertiesWithDetailsOutput expectedProperties = TestPropertyWithDetails.toPropertiesWithDetailsOutput(testPropertiesWithDetails);
            assertPropertiesDetails(expectedProperties);
        });
    }

    private void assertPropertiesDetails(PropertiesWithDetailsOutput expectedProperties) {
        assertOK();
        PropertiesWithDetailsOutput actualModuleProperties = testContext.getResponseBody();
        assertEquals(expectedProperties, actualModuleProperties);
    }

    @Value
    private static class TestPropertyWithDetails {
        String name;
        String storedValue;
        String finalValue;
        String defaultValue;
        String transformations;

        static PropertiesWithDetailsOutput toPropertiesWithDetailsOutput(List<TestPropertyWithDetails> testPropertiesWithDetails) {
            List<PropertyWithDetailsOutput> propertyWithDetailsOutputs = testPropertiesWithDetails
                    .stream()
                    .map(TestPropertyWithDetails::toPropertyWithDetailsOutput)
                    .collect(Collectors.toList());

            return new PropertiesWithDetailsOutput(propertyWithDetailsOutputs);
        }

        private PropertyWithDetailsOutput toPropertyWithDetailsOutput() {
            return new PropertyWithDetailsOutput(
                    name,
                    defaultIfEmpty(storedValue, null),
                    finalValue,
                    defaultValue,
                    getTransformationsFromString()
            );
        }

        private ValuedPropertyTransformation[] getTransformationsFromString() {
            return Stream.of(this.transformations.split(","))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .map(ValuedPropertyTransformation::valueOf)
                    .toArray(ValuedPropertyTransformation[]::new);
        }
    }
}
