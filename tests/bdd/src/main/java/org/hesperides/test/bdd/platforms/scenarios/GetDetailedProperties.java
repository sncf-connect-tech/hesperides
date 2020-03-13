package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import lombok.Value;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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

        Then("^the detailed properties of(?: this)? module(?: \"([^\"]+)\")?(?: in logical group \"([^\"]+)\")? are$", (String moduleName, String logicalGroup, DataTable data) -> {
            assertOK();
            List<ModuleDetailedProperty> moduleDetailedProperties = data.asList(ModuleDetailedProperty.class);
            String propertiesPath = isNotEmpty(moduleName) ? platformBuilder.findDeployedModuleBuilder(moduleName, null, logicalGroup).buildPropertiesPath() : deployedModuleBuilder.buildPropertiesPath();
            List<ModuleDetailedPropertyOutput> expectedProperties = ModuleDetailedProperty.toModuleDetailedPropertyOutputs(moduleDetailedProperties, propertiesPath, platformBuilder);
            List<ModuleDetailedPropertyOutput> actualProperties = testContext.getResponseBody(PlatformDetailedPropertiesOutput.class).getDetailedProperties()
                    .stream()
                    .filter(property -> property.getPropertiesPath().equals(propertiesPath))
                    .collect(Collectors.toUnmodifiableList());
            assertEquals(expectedProperties, actualProperties);
        });

        Then("^the detailed global properties of this platform are$", (DataTable data) -> {
            assertOK();
            List<DetailedPropertyOutput> expectedProperties = data.asList(DetailedPropertyOutput.class);
            PlatformDetailedPropertiesOutput platformDetailedPropertiesOutput = testContext.getResponseBody();
            List<DetailedPropertyOutput> actualProperties = platformDetailedPropertiesOutput.getGlobalProperties();
            assertEquals(expectedProperties, actualProperties);
        });
    }

    @Value
    public static class ModuleDetailedProperty {
        String name;
        String storedValue;
        String finalValue;
        String defaultValue;
        boolean isRequired;
        boolean isPassword;
        String pattern;
        String comment;
        String referencedGlobalProperties;
        boolean isUnused;

        public static List<ModuleDetailedPropertyOutput> toModuleDetailedPropertyOutputs(List<ModuleDetailedProperty> moduleDetailedProperties, String propertiesPath, PlatformBuilder platformBuilder) {
            return moduleDetailedProperties.stream()
                    .map(moduleDetailedProperty -> moduleDetailedProperty.toModuleDetailedPropertyOutput(propertiesPath, platformBuilder))
                    .collect(toList());
        }

        public ModuleDetailedPropertyOutput toModuleDetailedPropertyOutput(String propertiesPath, PlatformBuilder platformBuilder) {
            return new ModuleDetailedPropertyOutput(
                    name,
                    defaultIfEmpty(storedValue, null),
                    finalValue,
                    defaultIfEmpty(defaultValue, null),
                    isRequired,
                    isPassword,
                    defaultIfEmpty(pattern, null),
                    defaultIfEmpty(comment, null),
                    propertiesPath,
                    buildReferencedGlobalPropertiesOutput(platformBuilder),
                    isUnused);
        }

        private List<DetailedPropertyOutput> buildReferencedGlobalPropertiesOutput(PlatformBuilder platformBuilder) {
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
    }
}
