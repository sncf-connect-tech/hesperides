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
package oldplatformscenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleHistory;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SaveProperties extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private OldModuleHistory moduleHistory;

    public SaveProperties() {

        Given("^I( try to)? save these iterable properties$", (String tryTo, DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = dataTableToIterableProperties(data.asList(IterableProperty.class));
            oldPlatformBuilder.withIterableProperties(iterableProperties);
            testContext.setResponseEntity(oldPlatformClient.saveProperties(
                    oldPlatformBuilder.buildInput(),
                    oldPlatformBuilder.getPropertiesIO(false),
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class)));
        });

        When("^I( try to)? save these properties$", (String tryTo, DataTable data) -> {
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            moduleBuilder.withValuedProperties(valuedProperties);
            testContext.setResponseEntity(oldPlatformClient.saveProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    getResponseType(tryTo, PropertiesIO.class)));
            moduleBuilder.incrementPropertiesVersionId();
        });

        When("^I update the properties of those modules one after the other using the same platform version_id$", () -> {
            moduleHistory.getModuleBuilders().forEach(moduleBuilder -> {
                moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
                testContext.setResponseEntity(oldPlatformClient.updateProperties(
                        oldPlatformBuilder.buildInput(),
                        moduleBuilder.buildPropertiesIO(),
                        moduleBuilder.getPropertiesPath(),
                        PropertiesIO.class));
                moduleBuilder.incrementPropertiesVersionId();
                oldPlatformBuilder.incrementVersionId();
            });
        });

        When("^I update this module properties$", () -> {
            moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
            oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    PropertiesIO.class);
            moduleBuilder.incrementPropertiesVersionId();
            oldPlatformBuilder.incrementVersionId();
        });

        When("^I update the module properties and then the platform global properties using the same platform version_id$", () -> {
            moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
            oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    PropertiesIO.class);
            assertOK();
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(true));
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    "#",
                    PropertiesIO.class));
            moduleBuilder.incrementPropertiesVersionId();
            oldPlatformBuilder.incrementVersionId();
        });

        When("^I try to update the module properties and then the platform using the same platform version_id$", () -> {
            final PlatformIO platformInput = oldPlatformBuilder.buildInput();
            moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    platformInput,
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    PropertiesIO.class));
            assertOK();
            moduleBuilder.incrementPropertiesVersionId();
            testContext.setResponseEntity(oldPlatformClient.update(platformInput, false, String.class));
        });

        When("^I try to update the properties of this module twice with the same properties version_id$", () -> {
            moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
//            moduleBuilder.incrementPropertiesVersionId();
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    PropertiesIO.class));
            assertOK();
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    String.class));
        });

        When("^I try to update global properties twice with the same global properties version_id$", () -> {
//            platformBuilder.incrementGlobalPropertiesVersionId();
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(oldPlatformBuilder.getGlobalPropertiesVersionId()),
                    "#",
                    PropertiesIO.class));
            assertOK();
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(oldPlatformBuilder.getGlobalPropertiesVersionId()),
                    "#",
                    String.class));
        });

        When("^I update this platform's global properties$", () -> {
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(oldPlatformBuilder.getGlobalPropertiesVersionId()),
                    "#",
                    String.class));
            assertOK();
        });

        When("^I try to update the properties with wrong platform_version_id and without properties_version_id$", () -> {
            moduleBuilder.setPropertiesVersionId(null);
            oldPlatformBuilder.incrementVersionId();
            testContext.setResponseEntity(oldPlatformClient.updateProperties(
                    oldPlatformBuilder.buildInput(),
                    moduleBuilder.buildPropertiesIO(),
                    moduleBuilder.getPropertiesPath(),
                    String.class));
        });

        Then("^the properties are successfully saved$", () -> {
            assertOK();
            PropertiesIO expectedProperties = moduleBuilder.buildPropertiesIO();
            PropertiesIO actualProperties = testContext.getResponseBody(PropertiesIO.class);
            assertEquals(expectedProperties, actualProperties);
        });

        Then("^the properties are successfully saved for those modules$", () -> {
            moduleHistory.getModuleBuilders().forEach(moduleBuilder -> {
                moduleBuilder.withValuedProperties(oldPlatformBuilder.getValuedProperties(false));
                PropertiesIO expectedProperties = moduleBuilder.buildPropertiesIO();
                PropertiesIO actualProperties = oldPlatformClient.getProperties(oldPlatformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
                assertEquals(expectedProperties, actualProperties);
            });
        });

        Then("^the properties versionId should stay the same$", () -> {
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            Long expectedPropertiesVersionId = moduleBuilder.getPropertiesVersionId();
            Long actualPropertiesVersionId = actualPlatform.getDeployedModules().get(0).getPropertiesVersionId();
            assertEquals(expectedPropertiesVersionId, actualPropertiesVersionId);
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
                items.add(new IterablePropertyItemIO(itemTitle, new ArrayList<>(properties)));
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
