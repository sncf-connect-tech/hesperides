/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java8.En;
import lombok.Value;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hesperides.test.bdd.commons.DataTableHelper.decodeValue;
import static org.junit.Assert.assertEquals;

public class SaveProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;

    public SaveProperties() {

        Given("^(?:the module \"([^\"]+)\"|the platform(?: \"([^\"]+)\")?)" +
                "(?: in version \"([^\"]+)\")? " +
                "has these (valued|global|instance|iterable)? properties" +
                "(?: for the logical group \"([^\"]+)\")?$", (
                String moduleName, String platformName, String moduleVersion, String propertiesNature, String logicalGroup, DataTable dataTable) -> {

            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, platformBuilder.getPlatformName());
            }

            // Possibilité de surcharger la variable membre dans le cas où c'est un module précis qui nous intéresse
            DeployedModuleBuilder deployedModuleBuilder = isEmpty(moduleName)
                    ? this.deployedModuleBuilder
                    : platformBuilder.findDeployedModuleBuilder(moduleName, moduleVersion, logicalGroup);

            switch (propertiesNature) {
                case "global":
                    platformBuilder.setGlobalProperties(dataTable.asList(ValuedPropertyIO.class));
                    saveGlobalProperties();

                    break;
                case "instance":
                    instanceBuilder.setValuedProperties(dataTable.asList(ValuedPropertyIO.class));
                    saveInstanceProperties();

                    break;
                case "iterable":
                    deployedModuleBuilder.setIterableProperties(dataTableToIterableProperties(dataTable));
                    saveValuedProperties(deployedModuleBuilder);

                    break;
                default: // Correspond au cas des propriétés valorisées au niveau module
                    deployedModuleBuilder.clearValuedProperties();
                    List<ValuedPropertyIO> valuedProperties = dataTable.asList(ValuedPropertyIO.class);
                    valuedProperties.forEach(property -> deployedModuleBuilder.withValuedProperty(property.getName(), property.getValue()));
                    saveValuedProperties(deployedModuleBuilder);
                    break;
            }
        });

        Given("^the platform has nested iterable properties$", () -> {
            List<IterableValuedPropertyIO> iterableProperties = Collections.singletonList(
                    new IterableValuedPropertyIO("a", Collections.singletonList(
                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                    new ValuedPropertyIO("valued_in_a", "value_a"),
                                    new IterableValuedPropertyIO("b", Collections.singletonList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("valued_in_b", "value_b"),
                                                    new IterableValuedPropertyIO("c", Collections.singletonList(
                                                            new IterablePropertyItemIO("", new ArrayList<>(Collections.singletonList(
                                                                    new ValuedPropertyIO("valued_in_c", "value_c")
                                                            )))
                                                    ))
                                            )))
                                    )),
                                    new IterableValuedPropertyIO("d", Collections.singletonList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Collections.singletonList(
                                                    new ValuedPropertyIO("valued_in_d", "value_d")
                                            )))
                                    ))
                            )))
                    ))
            );
            deployedModuleBuilder.setIterableProperties(iterableProperties);
            saveValuedProperties();
        });

        Given("^the deployed module has properties with values referencing global properties$", () -> {
            deployedModuleBuilder.withValuedProperty("property-a", "{{global-module-foo}}");
            deployedModuleBuilder.withValuedProperty("property-b", "{{global-techno-foo}}");
            deployedModuleBuilder.withValuedProperty("property-c", "{{global-filename}}{{global-location}}");
            saveValuedProperties();
        });

        Given("^the deployed module has iterable properties with values referencing the global properties$", () -> {
            deployedModuleBuilder.withIterableProperty(new IterableValuedPropertyIO("iterable-property",
                    Collections.singletonList(new IterablePropertyItemIO("item", Arrays.asList(
                            new ValuedPropertyIO("property-a", "{{global-module-foo}}"),
                            new ValuedPropertyIO("property-b", "{{global-techno-foo}}"),
                            new ValuedPropertyIO("property-c", "{{global-filename}}{{global-location}}")
                    )))));
            saveValuedProperties();
        });

        When("^I( try to)? save these properties$", (String tryTo, DataTable dataTable) -> {
            deployedModuleBuilder.setValuedProperties(dataTable.asList(ValuedPropertyIO.class));
            saveValuedProperties(tryTo, deployedModuleBuilder);
        });

        When("^I( try to)? save these iterable properties$", (String tryTo, DataTable dataTable) -> {
            deployedModuleBuilder.setIterableProperties(dataTableToIterableProperties(dataTable));
            saveValuedProperties(tryTo, deployedModuleBuilder);
        });

        When("^I try to save a property declared twice with the same name but different values$", () -> {
            deployedModuleBuilder.withValuedProperty("property-a", "foo");
            deployedModuleBuilder.withValuedProperty("property-a", "bar");
            saveValuedProperties("should-fail", deployedModuleBuilder);
        });

        When("^I try to save a duplicate property that only differ by a trailing whitespace$", () -> {
            deployedModuleBuilder.withValuedProperty("property-a", "foo");
            deployedModuleBuilder.withValuedProperty("property-a ", "bar");
            saveValuedProperties("should-fail", deployedModuleBuilder);
        });

        Then("^the( global)? properties are successfully (?:sav|updat)ed$", (String globalProperties) -> {
            assertOK();
            if (isNotEmpty(globalProperties)) {
                assertGlobalProperties();
            } else {
                assertValuedProperties();
            }
        });
    }

    private void assertValuedProperties() {
        assertValuedProperties(deployedModuleBuilder);
    }

    void assertValuedProperties(DeployedModuleBuilder deployedModuleBuilder) {
        PropertiesIO expectedProperties = deployedModuleBuilder.buildProperties();
        PropertiesIO actualProperties = platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        assertEquals(expectedProperties, actualProperties);
    }

    void assertGlobalProperties() {
        PropertiesIO expectedProperties = platformBuilder.buildProperties();
        PropertiesIO actualProperties = platformClient.getGlobalProperties(platformBuilder.buildInput());
        assertEquals(expectedProperties, actualProperties);
    }

    void saveValuedProperties() {
        saveValuedProperties(null, deployedModuleBuilder);
    }

    void saveValuedProperties(DeployedModuleBuilder deployedModuleBuilder) {
        saveValuedProperties(null, deployedModuleBuilder);
    }

    private void saveValuedProperties(String tryTo, DeployedModuleBuilder deployedModuleBuilder) {
        platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath(), tryTo);
        if (isEmpty(tryTo)) {
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        }
    }

    void saveGlobalProperties() {
        platformClient.saveGlobalProperties(platformBuilder.buildInput(), platformBuilder.buildProperties());
        platformBuilder.incrementGlobalPropertiesVersionId();
        platformHistory.updatePlatformBuilder(platformBuilder);
    }

    void saveInstanceProperties() {
        deployedModuleBuilder.upsertInstanceBuilder(instanceBuilder);
        platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
        platformClient.updatePlatform(platformBuilder.buildInput());
        platformHistory.updatePlatformBuilder(platformBuilder);
    }

    @DataTableType
    public ValuedPropertyIO valuedPropertyIO(Map<String, String> entry) {
        return new ValuedPropertyIO(
                decodeValue(entry.get("name")),
                decodeValue(entry.get("value"))
        );
    }

    @DataTableType
    public IterableProperty iterableValuedPropertyIO(Map<String, String> entry) {
        return new IterableProperty(
                decodeValue(entry.get("iterable")),
                decodeValue(entry.get("bloc")),
                decodeValue(entry.get("name")),
                decodeValue(entry.get("value"))
        );
    }

    @Value
    public static class IterableProperty {
        String iterable;
        String bloc;
        String name;
        String value;
    }

    /**
     * Cette méthode transforme une matrice à deux dimensions
     * contenant les colonnes "iterable", "bloc", "name", "value"
     * en une liste de IterableValuedPropertyIO.
     */
    static List<IterableValuedPropertyIO> dataTableToIterableProperties(DataTable dataTable) {
        List<IterableProperty> iterablePropertiesData = dataTable.asList(IterableProperty.class);
        // Première étape : transformer la datatable en map
        // pour mutualiser les données
        Map<String, Map<String, Map<String, String>>> iterableMap = new HashMap<>();
        iterablePropertiesData.forEach(iterableProperty -> {
            Map<String, Map<String, String>> itemMap = iterableMap.getOrDefault(iterableProperty.getIterable(), new HashMap<>());
            Map<String, String> propertyMap = itemMap.getOrDefault(iterableProperty.getBloc(), new HashMap<>());
            propertyMap.put(iterableProperty.getName(), iterableProperty.getValue());
            itemMap.put(iterableProperty.getBloc(), propertyMap);
            iterableMap.put(iterableProperty.getIterable(), itemMap);
        });
        // Deuxième étape : recréer l'arbre des données
        // attendues en input à l'aide des maps
        List<IterableValuedPropertyIO> iterableProperties = new ArrayList<>();
        iterableMap.forEach((iterableName, iterableItems) -> {
            List<IterablePropertyItemIO> items = new ArrayList<>();
            iterableItems.forEach((itemTitle, itemProperties) -> {
                List<AbstractValuedPropertyIO> properties = new ArrayList<>();
                itemProperties.forEach((propertyName, propertyValue) -> properties.add(new ValuedPropertyIO(propertyName, propertyValue)));
                items.add(new IterablePropertyItemIO(itemTitle, new ArrayList<>(properties)));
            });
            iterableProperties.add(new IterableValuedPropertyIO(iterableName, items));
        });
        return iterableProperties;
    }
}
