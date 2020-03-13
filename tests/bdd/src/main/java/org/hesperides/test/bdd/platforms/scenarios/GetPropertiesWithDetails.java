package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java8.En;
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
import java.util.Map;
import java.util.stream.Stream;

import static org.hesperides.test.bdd.commons.DataTableHelper.decodeValue;
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

        Then("^the properties details are successfully retrieved and they are empty$", () -> assertProperties(Collections.emptyList()));

        Then("^the properties details match these values$", (DataTable dataTable) -> {
            List<PropertyWithDetailsOutput> expectedProperties = dataTable.asList(PropertyWithDetailsOutput.class);
            assertProperties(expectedProperties);
        });
    }

    private void assertProperties(List<PropertyWithDetailsOutput> expectedProperties) {
        assertOK();
        List<PropertyWithDetailsOutput> actualProperties = testContext.getResponseBody(PropertiesWithDetailsOutput.class).getValuedProperties();
        assertEquals(expectedProperties, actualProperties);
    }

    @DataTableType
    public PropertyWithDetailsOutput propertiesWithDetailsOutput(Map<String, String> entry) {
        return new PropertyWithDetailsOutput(
                decodeValue(entry.get("name")),
                entry.get("storedValue"),
                decodeValue(entry.get("finalValue")),
                decodeValue(entry.get("defaultValue")),
                buildValuedPropertyTransformation(decodeValue(entry.get("transformations")))
        );
    }

    private static ValuedPropertyTransformation[] buildValuedPropertyTransformation(String value) {
        return Stream.of(value.split(","))
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .map(ValuedPropertyTransformation::valueOf)
                .toArray(ValuedPropertyTransformation[]::new);
    }
}
