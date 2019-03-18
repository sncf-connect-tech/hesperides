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
import cucumber.api.java.en.Given;
import cucumber.api.java8.En;
import lombok.Value;
import org.hamcrest.Matcher;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.commons.CommonSteps;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Every.everyItem;
import static org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView.OBFUSCATED_PASSWORD_VALUE;
import static org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO.toDomainInstances;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreatePlatforms extends HesperidesScenario implements En {

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
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private CommonSteps commonSteps;

    @Given("^an existing( prod)? platform" +
            "(?: named \"([^\"]*)\")?" +
            "( with this module)?" +
            "(?: in logical group \"([^\"]*)\")?" +
            "( (?:and|with) an instance)?" +
            "( (?:and|with) valued properties)?" +
            "( (?:and|with) iterable properties)?" +
            "( (?:and|with) iterable-ception)?" +
            "( (?:and|with) global properties)?" +
            "( (?:and|with) instance properties)?" +
            "( (?:and|with) global properties as instance values)?" +
            "(?: (?:and|with) an instance value named \"([^ ]+)\")?" +
            "( (?:and|with) filename and location values)?$")
    public void givenAnExistingPlatform(String isProd,
                                        String platformName,
                                        String withThisModule,
                                        String logicalGroup,
                                        String withAnInstance,
                                        String withValuedProperties,
                                        String withIterableProperties,
                                        String withIterableCeption,
                                        String withGlobalProperties,
                                        String withInstanceProperties,
                                        String withGlobalPropertiesAsInstanceValues,
                                        String withInstanceValueNamed,
                                        String withFilenameLocationValues) {
        moduleBuilder.setLogicalGroup(logicalGroup);

        if (isNotEmpty(isProd)) {
            platformBuilder.withIsProductionPlatform(true);
            commonSteps.setAuthUserRole("prod");
        }

        if (isNotEmpty(platformName)) {
            platformBuilder.withPlatformName(platformName);
        }

        if (isNotEmpty(withThisModule)) {
            if (isNotEmpty(withAnInstance)) {
                if (isNotEmpty(withGlobalPropertiesAsInstanceValues)) {
                    platformBuilder.withInstancePropertyValue("instance-module-foo", "global-module-foo");
                }
                if (isNotEmpty(withInstanceValueNamed)) {
                    platformBuilder.withInstancePropertyValue(withInstanceValueNamed, "/var");
                }
                platformBuilder.withInstance("instance-foo-1");
            }
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
        }
        commonSteps.ensureUserAuthIsSet();
        testContext.responseEntity = platformClient.create(platformBuilder.buildInput());
        assertOK();

        if (isNotEmpty(withValuedProperties)) {
            modelBuilder.getProperties().forEach(property -> platformBuilder.withProperty(property.getName(), "12"));
            if (moduleBuilder.hasTechno()) {
                platformBuilder.withProperty("techno-foo", "12");
            }
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withIterableProperties)) {
            platformBuilder.withIterableProperties(Arrays.asList(
                    new IterableValuedPropertyIO("module-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-module-1", new HashSet<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-module-2", new HashSet<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-2"))))
                    )),
                    new IterableValuedPropertyIO("techno-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-techno-1", new HashSet<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-techno-2", new HashSet<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-2"))))
                    ))
            ));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withIterableCeption)) {
            platformBuilder.withIterableProperties(Arrays.asList(
                    new IterableValuedPropertyIO("module-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-module-foo-1", new HashSet<>(Arrays.asList(
                                    new IterableValuedPropertyIO("module-bar", Arrays.asList(
                                            new IterablePropertyItemIO("bloc-module-bar-1", new HashSet<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-1")
                                            ))),
                                            new IterablePropertyItemIO("bloc-module-bar-2", new HashSet<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-2")
                                            )))
                                    ))
                            ))),
                            new IterablePropertyItemIO("bloc-module-foo-2", new HashSet<>(Arrays.asList(
                                    new IterableValuedPropertyIO("module-bar", Arrays.asList(
                                            new IterablePropertyItemIO("bloc-module-bar-1", new HashSet<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-3")
                                            ))),
                                            new IterablePropertyItemIO("bloc-module-bar-2", new HashSet<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-4")
                                            )))
                                    ))
                            )))
                    ))
            ));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withGlobalProperties)) {
            platformBuilder.withGlobalProperty("global-module-foo", "12", modelBuilder);
            if (moduleBuilder.hasTechno()) {
                platformBuilder.withGlobalProperty("global-techno-foo", "12", modelBuilder);
            }
            platformBuilder.withGlobalProperty("global-filename", "abc", modelBuilder);
            platformBuilder.withGlobalProperty("global-location", "def", modelBuilder);
            platformBuilder.withGlobalProperty("unused-global-property", "12", modelBuilder);
            platformClient.saveGlobalProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(true));
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withInstanceProperties)) {
            platformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
            if (moduleBuilder.hasTechno()) {
                platformBuilder.withInstanceProperty("techno-foo", "instance-techno-foo");
            }
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withFilenameLocationValues)) {
            platformBuilder.withProperty("filename", "conf");
            platformBuilder.withProperty("location", "etc");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        platformHistory.addPlatform();
    }

    public CreatePlatforms() {

        Given("^a( prod)? platform to create(?:, named \"([^\"]*)\")?( with this module(?: associated to an empty path)?)?( with an instance( with properties)?)?( with the same name but different letter case)?$", (
                String isProd, String platformName, String withThisModule, String withAnInstance, String withProperties, String sameNameDifferentLetterCase) -> {

            if (isNotEmpty(isProd)) {
                platformBuilder.withIsProductionPlatform(true);
            }

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            } else if (isNotEmpty(sameNameDifferentLetterCase)) {
                platformBuilder.withPlatformName(platformBuilder.getPlatformName().toUpperCase());
            }

            if (isNotEmpty(withThisModule)) {
                if (isNotEmpty(withAnInstance)) {

                    List<ValuedPropertyIO> instancesProperties = new ArrayList<>();
                    if (isNotEmpty(withProperties)) {
                        instancesProperties.add(new ValuedPropertyIO("instance-property-a", "instance-property-a-val"));
                        instancesProperties.add(new ValuedPropertyIO("instance-property-b", "instance-property-b-val"));
                    }

                    platformBuilder.withInstance("instance-foo-1", instancesProperties);
                }
                moduleBuilder.setLogicalGroup(withThisModule.contains("empty path") ? "" : null);
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            }
        });

        Given("^the platform has instance properties with the same name as a global property$", () -> {
            platformBuilder.withProperty("module-foo", "{{ global-property }}");
            platformBuilder.withInstanceProperty("module-bar", "instance-property");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
            platformBuilder.withGlobalProperty("global-property", "12", modelBuilder);
            platformClient.saveGlobalProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(true));
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has instance properties with the same name as another module property$", () -> {
            platformBuilder.withProperty("module-foo", "{{ module-bar }}");
            platformBuilder.withProperty("module-bar", "12");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has instance properties with the same name as the module property that it's declared in$", () -> {
            platformBuilder.withInstanceProperty("module-foobar", "module-foobar");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has multiple instance properties declared in the same property value$", () -> {
            platformBuilder.withInstanceProperty("module-bar", "instance-property", "another-instance-property");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has an instance property declared twice$", () -> {
            platformBuilder.withInstanceProperty("module-foo", "instance-property");
            platformBuilder.withInstanceProperty("module-bar", "instance-property");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has these valued properties$", (DataTable data) -> {
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            valuedProperties.forEach(property -> platformBuilder.withProperty(property.getName(), property.getValue().replace("&nbsp;", " ")));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has these iterable properties$", (DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = dataTableToIterableProperties(data.asList(IterableProperty.class));
            platformBuilder.withIterableProperties(iterableProperties);
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has these global properties$", (DataTable data) -> {
            List<ValuedPropertyIO> globalProperties = data.asList(ValuedPropertyIO.class);
            platformClient.saveGlobalProperties(platformBuilder.buildInput(), new PropertiesIO(new HashSet<>(globalProperties), Collections.emptySet()));
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has these instance properties$", (DataTable data) -> {
            List<ValuedPropertyIO> instanceProperties = data.asList(ValuedPropertyIO.class);
            platformBuilder.withInstance("some-instance", instanceProperties);
            platformClient.update(platformBuilder.buildInput(), false, PlatformIO.class);
            platformBuilder.incrementVersionId();
        });

        When("^I( try to)? create this platform$", (String tryTo) -> {
            testContext.responseEntity = platformClient.create(platformBuilder.buildInput(), getResponseType(tryTo, PlatformIO.class));
        });

        When("^I( try to)? copy this platform( using the same key)?( to a non-prod one)?$", (String tryTo, String usingTheSameKey, String toNonProd) -> {
            PlatformIO existingPlatform = platformBuilder.buildInput();
            String newName = isNotEmpty(usingTheSameKey) ? existingPlatform.getPlatformName() : existingPlatform.getPlatformName() + "-copy";
            PlatformBuilder newPlatform = new PlatformBuilder()
                    .withApplicationName(existingPlatform.getApplicationName())
                    .withPlatformName(newName);
            if (isNotEmpty(toNonProd)) {
                newPlatform.withIsProductionPlatform(false);
            }
            testContext.responseEntity = platformClient.copy(existingPlatform, newPlatform.buildInput(), getResponseType(tryTo, PlatformIO.class));
            platformBuilder.withPlatformName(newName);
            platformBuilder.withVersionId(1);
        });

        Then("^the platform is successfully created(?: with \"(.*)\" as path)?$", (String expectedModulePath) -> {
            assertOK();
            // The returned deployed modules always have a non-empty modulePath, even if none was provided:
            if (platformBuilder.getDeployedModules().size() > 0 && isBlank(platformBuilder.getDeployedModules().get(0).getModulePath())) {
                platformBuilder.withNoModule();
                moduleBuilder.setLogicalGroup("#");
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            }
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
            if (isNotEmpty(expectedModulePath)) {
                Assert.assertEquals(expectedModulePath, actualPlatform.getDeployedModules().get(0).getModulePath());
            }
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), testContext.responseEntity.getStatusCode());
            assertThat((String) testContext.getResponseBody(), containsString(message));
        });

        Then("^the platform creation fails with an already exist error$", () -> {
            assertConflict();
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

        Then("^there are obfuscated password properties in the(?: initial)? file$", () -> {
            String actualOutput = (String) testContext.getResponseBody();
            assertThat(actualOutput, containsString(OBFUSCATED_PASSWORD_VALUE));
        });

        Then("^the platform copy fails with a not found error$", () -> {
            assertNotFound();
        });

        Then("^the platform copy fails with an already exist error$", () -> {
            assertConflict();
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

    /**
     * Cette méthode transforme une matrice à deux dimensions
     * contenant les colonnes "iterable", "bloc", "name", "value"
     * en une liste de IterableValuedPropertyIO.
     */
    private List<IterableValuedPropertyIO> dataTableToIterableProperties(List<IterableProperty> valuedProperties) {
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
        iterableMap.entrySet().forEach(iterable -> {
            List<IterablePropertyItemIO> items = new ArrayList<>();
            iterable.getValue().entrySet().forEach(bloc -> {
                List<AbstractValuedPropertyIO> properties = new ArrayList<>();
                bloc.getValue().entrySet().forEach(property -> {
                    properties.add(new ValuedPropertyIO(property.getKey(), property.getValue()));
                });
                items.add(new IterablePropertyItemIO(bloc.getKey(), new HashSet<>(properties)));
            });
            iterableProperties.add(new IterableValuedPropertyIO(iterable.getKey(), items));
        });
        return iterableProperties;
    }

    @Value
    private class IterableProperty {
        String iterable;
        String bloc;
        String name;
        String value;
    }
}
