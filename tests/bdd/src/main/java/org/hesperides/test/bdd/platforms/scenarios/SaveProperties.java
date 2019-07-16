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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class SaveProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    private PropertiesIO propertiesIO;

    public SaveProperties() {

        Given("^I( try to)? save these iterable properties$", (String tryTo, DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = dataTableToIterableProperties(data.asList(IterableProperty.class));
            platformBuilder.withIterableProperties(iterableProperties);
            testContext.responseEntity = platformClient.saveProperties(
                    platformBuilder.buildInput(),
                    platformBuilder.getPropertiesIO(false),
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class));
        });

        When("^I( try to)? save these properties$", (String tryTo, DataTable data) -> {
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            valuedProperties.forEach(property -> platformBuilder.withProperty(property.getName(), property.getValue()));
            propertiesIO = new PropertiesIO(0L, new HashSet<>(valuedProperties), Collections.emptySet());
            testContext.responseEntity = platformClient.saveProperties(
                    platformBuilder.buildInput(),
                    propertiesIO,
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class));
        });

        When("^I update the properties of those modules one after the other using the same platform version_id$", () -> {
            moduleHistory.getModuleBuilders().forEach(moduleBuilder -> {
                testContext.responseEntity = platformClient.updateProperties(
                        platformBuilder.buildInput(),
                        propertiesIO,
                        moduleBuilder.getPropertiesPath(),
                        PropertiesIO.class);
            });
        });

        When("^I update the module properties and then the platform using the same platform version_id$", () -> {
            final PlatformIO platformInput = platformBuilder.buildInput();
            platformClient.updateProperties(
                    platformInput,
                    propertiesIO,
                    moduleBuilder.getPropertiesPath(),
                    PropertiesIO.class);
            testContext.responseEntity = platformClient.update(platformInput, false, String.class);
        });

        When("^I update the properties of this module twice with the same deployed module version_id$", () -> {
            for (int i = 0; i < 2; i++) {
                testContext.responseEntity = platformClient.updateProperties(
                        platformBuilder.buildInput(),
                        propertiesIO,
                        moduleBuilder.getPropertiesPath(),
                        PropertiesIO.class);
            }
        });

        Then("^the properties are successfully saved$", () -> {
            assertOK();
            PropertiesIO expectedProperties = propertiesIO;
            PropertiesIO actualProperties = (PropertiesIO) testContext.getResponseBody();
            assertEquals(expectedProperties, actualProperties);
        });

        Then("^the properties update is rejected with a conflict error$", this::assertConflict);
    }

    /**
     * Cette méthode transforme une matrice à deux dimensions
     * contenant les colonnes "iterable", "bloc", "name", "value"
     * en une liste de IterableValuedPropertyIO.
     */
    static List<IterableValuedPropertyIO> dataTableToIterableProperties(List<IterableProperty> valuedProperties) {
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
                items.add(new IterablePropertyItemIO(itemTitle, new HashSet<>(properties)));
            });
            iterableProperties.add(new IterableValuedPropertyIO(iterableName, items));
        });
        return iterableProperties;
    }

    @Value
    public static class IterableProperty {
        String iterable;
        String bloc;
        String name;
        String value;
    }
}
