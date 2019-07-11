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
import org.hesperides.test.bdd.modules.ModuleBuilder;
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

    private PropertiesIO propertiesIO;

    public SaveProperties() {

        When("^I( try to)? save these properties?$", (String tryTo, DataTable data) -> {
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            valuedProperties.forEach(property -> platformBuilder.withProperty(property.getName(), property.getValue()));
            propertiesIO = new PropertiesIO(new HashSet<>(valuedProperties), Collections.emptySet());
            testContext.setResponseEntity(platformClient.saveProperties(
                    platformBuilder.buildInput(),
                    propertiesIO,
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class)));
        });

        Given("^I( try to)? save these iterable properties$", (String tryTo, DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = dataTableToIterableProperties(data.asList(IterableProperty.class));
            platformBuilder.withIterableProperties(iterableProperties);
            testContext.setResponseEntity(platformClient.saveProperties(
                    platformBuilder.buildInput(),
                    platformBuilder.getPropertiesIO(false),
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class)));
        });

        Then("^the properties are successfully saved$", () -> {
            assertOK();
            PropertiesIO expectedProperties = propertiesIO;
            PropertiesIO actualProperties = testContext.getResponseBody(PropertiesIO.class);
            assertEquals(expectedProperties, actualProperties);
        });
    }

    /**
     * Cette méthode transforme une matrice à deux dimensions
     * contenant les colonnes "iterable", "bloc", "name", "value"
     * en une liste de IterableValuedPropertyIO.
     */
    public static List<IterableValuedPropertyIO> dataTableToIterableProperties(List<IterableProperty> valuedProperties) {
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
