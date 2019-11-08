package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertyWithDetailsIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

enum AttributeDetailsType {
    NAME,
    STORED_VALUE,
    FINAL_VALUE,
    DEFAULT_VALUE
}

public class GetPropertiesWithDetails extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public GetPropertiesWithDetails() {

        When("^I get the properties with details of this platforms$", () -> {
            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        When("^I get the platform properties with details for this module$", () -> {
            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        Then("^the properties with theirs details are successfully retrieved$", () -> {

            assertOK();

            Long timestamp = platformHistory.getPlatformFirstTimestamp(platformBuilder.getApplicationName(), platformBuilder.getPlatformName());
            DeployedModuleBuilder deployedModuleBuilder = platformHistory.getFirstPlatformBuilder(platformBuilder.getApplicationName(),
                    platformBuilder.getPlatformName()).getDeployedModuleBuilders().get(0);
            PropertiesIO expectedModuleProperties = platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath(), timestamp);
            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            assertEquals(expectedModuleProperties, actualModuleProperties);
        });

        Then("^the properties with details and its contain are successfully retrieved$", () -> {
            assertOK();
            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        Then("^the properties details match these values$", (DataTable data) -> {

            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            Set<PropertyWithDetailsIO> actualProperties = actualModuleProperties.getValuedProperties();
            Set<Details> providedProperties = new HashSet<>(data.asList(Details.class));

            Set<String> expectedNames = Details.getNames(providedProperties);
            Set<String> actualNames = getPropertiesType(actualProperties, AttributeDetailsType.NAME);
            assertEquals("names", expectedNames, actualNames);

            Set<String> expectedStoredValues = Details.getStoredValues(providedProperties);
            Set<String> actualStoredValues = getPropertiesType(actualProperties, AttributeDetailsType.STORED_VALUE);
            assertEquals("stored_values", expectedStoredValues, actualStoredValues);

            Set<String> expectedFinalValues = Details.getFinalValues(providedProperties);
            Set<String> actualFinalsValues = getPropertiesType(actualProperties, AttributeDetailsType.FINAL_VALUE);
            assertEquals("final_values", expectedFinalValues, actualFinalsValues);

            Set<String> expectedDefaultValues = Details.getDefaultValues(providedProperties);
            Set<String> actualDefaultValues = getPropertiesType(actualProperties, AttributeDetailsType.DEFAULT_VALUE);
            assertEquals("default_values", expectedDefaultValues, actualDefaultValues);
        });
    }

    private Set<String> getPropertiesType(Set<PropertyWithDetailsIO> propertyWithDetails, AttributeDetailsType detailsType) {

        Set<String> propertiesType;
        switch (detailsType) {
            case NAME:
                propertiesType = propertyWithDetails.stream().map(PropertyWithDetailsIO::getName).collect(Collectors.toSet());
                break;
            case DEFAULT_VALUE:
                propertiesType = propertyWithDetails.stream()
                        .filter(propertyWithDetailsIO -> StringUtils.isNotBlank(propertyWithDetailsIO.getDefaultValue()))
                        .map(PropertyWithDetailsIO::getDefaultValue)
                        .collect(Collectors.toSet());
                break;
            case FINAL_VALUE:
                propertiesType = propertyWithDetails
                        .stream()
                        .filter(propertyWithDetailsIO -> StringUtils.isNotBlank(propertyWithDetailsIO.getFinalValue()))
                        .map(PropertyWithDetailsIO::getFinalValue)
                        .collect(Collectors.toSet());
                break;
            default:
                propertiesType = propertyWithDetails.stream()
                        .filter(propertyWithDetailsIO -> StringUtils.isNotBlank(propertyWithDetailsIO.getStoredValue()))
                        .map(PropertyWithDetailsIO::getStoredValue)
                        .collect(Collectors.toSet());
                break;
        }
        return propertiesType;
    }

    @Value
    private static class Details {

        String name;
        String storedValue;
        String finalValue;
        String defaultValue;
        ValuedPropertyTransformation[] transformations;

        private static Set<String> getNames(Set<Details> details) {
            return getTypeOfDetails(details, Details::getName);
        }

        private static Set<String> getStoredValues(Set<Details> details) {
            return getTypeOfDetails(details, Details::getStoredValue);
        }

        private static Set<String> getFinalValues(Set<Details> details) {
            return getTypeOfDetails(details, Details::getFinalValue);
        }

        private static Set<String> getDefaultValues(Set<Details> details) {
            return getTypeOfDetails(details, Details::getDefaultValue);
        }

        private static Set<String> getTypeOfDetails(Set<Details> details, Function<Details, String> mapper) {
            return details.stream().map(mapper).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        }
    }
}