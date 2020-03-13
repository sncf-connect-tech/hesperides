package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.platforms.properties.PlatformDetailedPropertiesOutput;
import org.hesperides.core.presentation.io.platforms.properties.PlatformDetailedPropertiesOutput.DetailedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.PlatformDetailedPropertiesOutput.ModuleDetailedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hesperides.test.bdd.commons.DataTableHelper.decodeValue;
import static org.junit.Assert.assertEquals;

public class GetDetailedProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;

    public GetDetailedProperties() {

        When("^I get the detailed properties of this (module|platform)?$", (String moduleOrPlatform) -> {
            String propertiesPath = "module".equals(moduleOrPlatform) ? deployedModuleBuilder.buildPropertiesPath() : null;
            platformClient.getDetailedProperties(platformBuilder.buildInput(), propertiesPath);
        });

        Then("^the detailed properties of(?: this)? module(?: \"([^\"]+)\")?(?: in logical group \"([^\"]+)\")? are$", (
                String moduleName, String logicalGroup, DataTable dataTable) -> {
            assertOK();
            String propertiesPath = isEmpty(moduleName)
                    ? deployedModuleBuilder.buildPropertiesPath()
                    : platformBuilder.findDeployedModuleBuilder(moduleName, null, logicalGroup).buildPropertiesPath();

            List<ModuleDetailedPropertyOutput> expectedProperties = toModuleDetailedPropertyOutputs(
                    dataTable.asMaps(String.class, String.class), propertiesPath);

            List<ModuleDetailedPropertyOutput> actualProperties = testContext.getResponseBody(PlatformDetailedPropertiesOutput.class)
                    .getDetailedProperties()
                    .stream()
                    .filter(property -> property.getPropertiesPath().equals(propertiesPath))
                    .collect(Collectors.toUnmodifiableList());

            assertEquals(expectedProperties, actualProperties);
        });

        Then("^the detailed global properties of this platform are$", (DataTable dataTable) -> {
            assertOK();
            List<DetailedPropertyOutput> expectedProperties = dataTable.asList(DetailedPropertyOutput.class);
            PlatformDetailedPropertiesOutput platformDetailedPropertiesOutput = testContext.getResponseBody();
            List<DetailedPropertyOutput> actualProperties = platformDetailedPropertiesOutput.getGlobalProperties();
            assertEquals(expectedProperties, actualProperties);
        });
    }

    private List<ModuleDetailedPropertyOutput> toModuleDetailedPropertyOutputs(List<Map<String, String>> data, String propertiesPath) {
        return data.stream()
                .map(entry -> toModuleDetailedPropertyOutput(entry, propertiesPath))
                .collect(toList());
    }

    private ModuleDetailedPropertyOutput toModuleDetailedPropertyOutput(Map<String, String> entry, String propertiesPath) {
        return new ModuleDetailedPropertyOutput(
                decodeValue(entry.get("name")),
                entry.get("stored_value"),
                decodeValue(entry.get("final_value")),
                entry.get("default_value"),
                Boolean.parseBoolean(decodeValue(entry.get("is_required"))),
                Boolean.parseBoolean(decodeValue(entry.get("is_password"))),
                entry.get("pattern"),
                entry.get("comment"),
                propertiesPath,
                buildReferencedGlobalPropertiesOutput(entry.get("referenced_global_properties")),
                Boolean.parseBoolean(decodeValue(entry.get("is_unused"))));
    }

    private List<DetailedPropertyOutput> buildReferencedGlobalPropertiesOutput(String referencedGlobalProperties) {
        List<DetailedPropertyOutput> referencedGlobalPropertiesOutput = new ArrayList<>();
        if (isNotEmpty(referencedGlobalProperties)) {
            referencedGlobalPropertiesOutput = Arrays.stream(referencedGlobalProperties.split(","))
                    .map(String::trim)
                    .map(referencedGlobalProperty -> {
                        ValuedPropertyIO globalProperty = platformBuilder.getGlobalProperties().stream()
                                .filter(platformProperty -> referencedGlobalProperty.equals(platformProperty.getName()))
                                .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't find referenced global property \"" + referencedGlobalProperty + "\""));

                        return new DetailedPropertyOutput(globalProperty.getName(), globalProperty.getValue(), globalProperty.getValue());
                    })
                    .collect(toList());
        }
        return referencedGlobalPropertiesOutput;
    }


    @DataTableType
    public DetailedPropertyOutput detailedPropertyOutput(Map<String, String> entry) {
        return new DetailedPropertyOutput(
                decodeValue(entry.get("name")),
                entry.get("stored_value"),
                decodeValue(entry.get("final_value")));
    }
}
