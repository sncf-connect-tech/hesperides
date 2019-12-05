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

import cucumber.api.DataTable;
import cucumber.api.java8.En;
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

        When("^I( try to)? save these properties$", (String tryTo, DataTable data) -> {
            deployedModuleBuilder.setValuedProperties(data.asList(ValuedPropertyIO.class));
            saveValuedProperties(tryTo, deployedModuleBuilder);
        });

        When("^I( try to)? save these iterable properties$", (String tryTo, DataTable data) -> {
            deployedModuleBuilder.setIterableProperties(dataTableToIterableProperties(data));
            saveValuedProperties(tryTo, deployedModuleBuilder);
        });

        Then("^the( global)? properties are successfully (?:sav|updat)ed$", (String globalProperties) -> {
            assertOK();
            if (isNotEmpty(globalProperties)) {
                assertGlobalProperties();
            } else {
                assertValuedProperties();
            }
        });

        When("^I try to save a property declared twice with the same name but different values$", () -> {
            deployedModuleBuilder.withValuedProperty("property-a", "foo");
            deployedModuleBuilder.withValuedProperty("property-a", "bar");
            saveValuedProperties("should-fail", deployedModuleBuilder);
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

    @Value
    private static class IterableProperty {
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
    static List<IterableValuedPropertyIO> dataTableToIterableProperties(DataTable data) {
        List<IterableProperty> valuedProperties = data.asList(IterableProperty.class);

        Map<String, Map<String, Map<String, String>>> iterableMap = new HashMap<>();
        // Première étape : transformer la datatable en map
        // pour mutualiser les données
        valuedProperties.forEach(iterableProperty -> {
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
