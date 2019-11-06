package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertyWithDetailsIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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

            List<PropertyWithDetailsIO> providedProperties = new ArrayList<>(data.asList(PropertyWithDetailsIO.class));
            List<PropertyWithDetailsIO> expectedModuleProperties = replaceBlankPropertiesWithNull(providedProperties);
            expectedModuleProperties.sort(Comparator.comparing(PropertyWithDetailsIO::getName));
            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            List<PropertyWithDetailsIO> actualProperties = new ArrayList<>(actualModuleProperties.getValuedProperties());
            actualProperties.sort(Comparator.comparing(PropertyWithDetailsIO::getName));
            assertEquals(expectedModuleProperties, actualProperties);
        });
    }

    private static List<PropertyWithDetailsIO> replaceBlankPropertiesWithNull(List<PropertyWithDetailsIO> providedPropertyWithDetails) {
        return providedPropertyWithDetails.stream()
                .map(propertyWithDetailsIO -> replaceBlankPropertiesWithNull(propertyWithDetailsIO))
                .collect(Collectors.toList());
    }

    private static String replaceBlankPropertiesWithNull(String property) {
        return StringUtils.isBlank(property) ? null : property;
    }

    // Only way i found to manage the null property values of cucumber Datatable
    private static PropertyWithDetailsIO replaceBlankPropertiesWithNull(PropertyWithDetailsIO propertyWithDetailsIO) {
        return new PropertyWithDetailsIO(replaceBlankPropertiesWithNull(propertyWithDetailsIO.getName()),
                replaceBlankPropertiesWithNull(propertyWithDetailsIO.getStoredValue()),
                replaceBlankPropertiesWithNull(propertyWithDetailsIO.getFinalValue()),
                propertyWithDetailsIO.getDefaultValue(), propertyWithDetailsIO.getTransformations());
    }
}