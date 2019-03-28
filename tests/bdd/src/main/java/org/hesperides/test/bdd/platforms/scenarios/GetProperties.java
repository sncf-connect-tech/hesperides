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

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Every.everyItem;
import static org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView.OBFUSCATED_PASSWORD_VALUE;
import static org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO.toDomainInstances;
import static org.junit.Assert.*;

public class GetProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetProperties() {

        When("^I get the platform properties for this module( at a specific time in the past)?$", (String withTimestamp) -> {
            Long timestamp = null;
            if (StringUtils.isNotEmpty(withTimestamp)) {
                timestamp = platformHistory.getFirstPlatformTimestamp();
            }
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath(), timestamp);
        });

        When("^I get the global properties of this platform$", () -> {
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), "#");
        });

        Then("^the platform property values are(?: also)? copied$", () -> {
            // Propriétés valorisées
            ResponseEntity<PropertiesIO> responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
            PropertiesIO expectedProperties = platformBuilder.getPropertiesIO(false);
            PropertiesIO actualProperties = responseEntity.getBody();
            assertThat(actualProperties.getValuedProperties(), containsInAnyOrder(expectedProperties.getValuedProperties().toArray()));
            assertThat(actualProperties.getIterableValuedProperties(), containsInAnyOrder(expectedProperties.getIterableValuedProperties().toArray()));
            // Propriétés globales
            responseEntity = platformClient.getProperties(platformBuilder.buildInput(), "#");
            assertOK();
            PropertiesIO expectedGlobalProperties = platformBuilder.getPropertiesIO(true);
            PropertiesIO actualGlobalProperties = responseEntity.getBody();
            assertEquals(expectedGlobalProperties, actualGlobalProperties);
        });

        Then("^the (password |non-password |)property values are (not )?obfuscated$", (String selectPasswordProps, String notObfuscated) -> {
            Matcher<String> isObfusctedOrNot = isNotEmpty(notObfuscated) ? not(equalTo(OBFUSCATED_PASSWORD_VALUE)) : equalTo(OBFUSCATED_PASSWORD_VALUE);

            ModelOutput model = (ModelOutput) moduleClient.getModel(moduleBuilder.build(), ModelOutput.class).getBody();
            Map<String, PropertyOutput> propertyModelsPerName = model.getProperties().stream().collect(Collectors.toMap(PropertyOutput::getName, Function.identity()));
            PropertiesIO actualProperties = (PropertiesIO) testContext.getResponseBody();

            List<ValuedProperty> actualDomainProperties = toDomainInstances(actualProperties.getValuedProperties());
            List<String> actualPropertyValues = isBlank(selectPasswordProps) ? extractValues(actualDomainProperties) : extractValuesIfPasswordOrNot(actualDomainProperties, propertyModelsPerName, "password ".equals(selectPasswordProps));
            assertThat(actualPropertyValues, hasSize(greaterThan(0)));
            assertThat(actualPropertyValues, everyItem(isObfusctedOrNot));

            List<ValuedProperty> actualIterableDomainProperties = flattenValuedProperties(actualProperties.getIterableValuedProperties());
            List<String> actualIterablePropertiesValues = isBlank(selectPasswordProps) ? extractValues(actualDomainProperties) : extractValuesIfPasswordOrNot(actualIterableDomainProperties, propertyModelsPerName, "password ".equals(selectPasswordProps));
            assertThat(actualIterablePropertiesValues, everyItem(isObfusctedOrNot));
        });

        Then("^the( initial)? platform( global)? properties are successfully retrieved$", (String initial, String global) -> {
            assertOK();
            PropertiesIO expectedProperties = StringUtils.isNotEmpty(initial) ? platformHistory.getInitialPlatformProperties() : platformBuilder.getPropertiesIO(StringUtils.isNotEmpty(global));
            PropertiesIO actualProperties = ((ResponseEntity<PropertiesIO>)testContext.responseEntity).getBody();
            assertEquals(expectedProperties, actualProperties);
        });

        Then("^property \"([^\"]*)\" has for value \"([^\"]*)\" on the platform$", (String propertyName, String expectedValue) -> {
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
            assertOK();
            PropertiesIO actualProperties = ((ResponseEntity<PropertiesIO>)testContext.responseEntity).getBody();
            Optional<ValuedPropertyIO> matchingProperty = actualProperties.getValuedProperties().stream().filter(property -> property.getName().equals(propertyName)).findFirst();
            assertTrue(matchingProperty.isPresent());
            assertEquals(expectedValue, matchingProperty.get().getValue());
        });

        Then("^property \"([^\"]*)\" has no value on the platform$", (String propertyName) -> {
            testContext.responseEntity = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
            assertOK();
            PropertiesIO actualProperties = ((ResponseEntity<PropertiesIO>)testContext.responseEntity).getBody();
            Optional<ValuedPropertyIO> matchingProperty = actualProperties.getValuedProperties().stream().filter(property -> property.getName().equals(propertyName)).findFirst();
            assertFalse(matchingProperty.isPresent());
        });
    }

    private static List<String> extractValues(List<ValuedProperty> valuedProperties) {
        return valuedProperties.stream()
                .map(ValuedProperty::getValue).collect(Collectors.toList());
    }

    private static List<String> extractValuesIfPasswordOrNot(List<ValuedProperty> valuedProperties, Map<String, PropertyOutput> propertyModelsPerName, boolean selectPasswordProps) {
        return valuedProperties.stream()
                .filter(p -> (!selectPasswordProps) ^ propertyModelsPerName.get(p.getName()).isPassword())
                .map(ValuedProperty::getValue).collect(Collectors.toList());
    }

    private static List<ValuedProperty> flattenValuedProperties(Set<IterableValuedPropertyIO> iterableValuedIOProperties) {
        List<AbstractValuedProperty> iterableValuedProperties = iterableValuedIOProperties.stream()
                .map(IterableValuedPropertyIO::toDomainInstance)
                .collect(Collectors.toList());
        return AbstractValuedProperty.getFlatValuedProperties(iterableValuedProperties);
    }
}
