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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.AuthorizationCredentialsConfig;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.users.UserAuthorities;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private UserAuthorities userAuthorities;

    @Given("^an existing( prod)? platform" +
            "(?: named \"([^\"]*)\")?" +
            "( with this module)?" +
            "( with two modules : one with the same name and one with the same version)?" +
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
                                        String withTwoModulesOneWithTheSameNameAndOneWithTheSameVersion,
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
        platformBuilder.reset();
        moduleBuilder.setLogicalGroup(logicalGroup);

        if (isNotEmpty(isProd)) {
            platformBuilder.withIsProductionPlatform(true);
            userAuthorities.setAuthUserRole(AuthorizationCredentialsConfig.PROD_TEST_PROFILE);
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
        if (isNotEmpty(withTwoModulesOneWithTheSameNameAndOneWithTheSameVersion)) {
            String origName = moduleBuilder.getName();
            String origVersion = moduleBuilder.getVersion();
            moduleBuilder.withName(origName + "2");
            moduleBuilder.withVersion(origVersion);
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion + "-SNAPSHOT");
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
            // On restaure les noms & versions :
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion);
        }
        if (isNotEmpty(withTwoModulesOneWithTheSameNameAndOneWithTheSameVersion)) {
            String origName = moduleBuilder.getName();
            String origVersion = moduleBuilder.getVersion();
            moduleBuilder.withName(origName + "2");
            moduleBuilder.withVersion(origVersion);
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion + "-SNAPSHOT");
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
            // On restaure les noms & versions :
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion);
        }
        userAuthorities.ensureUserAuthIsSet();
        testContext.setResponseEntity(platformClient.create(platformBuilder.buildInput()));
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
                            new IterablePropertyItemIO("bloc-module-1", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-module-2", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-2"))))
                    )),
                    new IterableValuedPropertyIO("techno-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-techno-1", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-techno-2", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-2"))))
                    ))
            ));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }

        if (isNotEmpty(withIterableCeption)) {
            platformBuilder.withIterableProperties(Arrays.asList(
                    new IterableValuedPropertyIO("module-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-module-foo-1", new ArrayList<>(Arrays.asList(
                                    new IterableValuedPropertyIO("module-bar", Arrays.asList(
                                            new IterablePropertyItemIO("bloc-module-bar-1", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-1")
                                            ))),
                                            new IterablePropertyItemIO("bloc-module-bar-2", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-2")
                                            )))
                                    ))
                            ))),
                            new IterablePropertyItemIO("bloc-module-foo-2", new ArrayList<>(Arrays.asList(
                                    new IterableValuedPropertyIO("module-bar", Arrays.asList(
                                            new IterablePropertyItemIO("bloc-module-bar-1", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("module-foobar", "module-foobar-val-3")
                                            ))),
                                            new IterablePropertyItemIO("bloc-module-bar-2", new ArrayList<>(Arrays.asList(
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

        Given("^a( prod)? platform to create" +
                "(?:, named \"([^\"]*)\")?" +
                "(?: with version \"([^\"]*)\")?" +
                "( with this module(?: associated to an empty path)?)?" +
                "( with an instance( with properties)?)?" +
                "( without setting isProductionPlatform?)?" +
                "( with the same name but different letter case)?$", (
                String isProd,
                String platformName,
                String platformVersion,
                String withThisModule,
                String withAnInstance,
                String withProperties,
                String withoutSettingIsProductionPlatform,
                String sameNameDifferentLetterCase) -> {

            if (isNotEmpty(isProd)) {
                platformBuilder.withIsProductionPlatform(true);
            }

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            } else if (isNotEmpty(sameNameDifferentLetterCase)) {
                platformBuilder.withPlatformName(platformBuilder.getPlatformName().toUpperCase());
            }

            if (isNotEmpty(platformVersion)) {
                platformBuilder.withVersion(platformVersion);
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

            if (isNotEmpty(withoutSettingIsProductionPlatform)) {
                platformBuilder.withIsProductionPlatform(null);
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

        Given("^the platform(?: \"([^\"]+)\")? has these valued properties$", (String platformName, DataTable data) -> {
            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, platformBuilder.getPlatformName());
            }
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            valuedProperties.forEach(property -> platformBuilder.withProperty(property.getName(), property.getValue().replace("&nbsp;", " ")));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        Given("^the platform has these iterable properties$", (DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = SaveProperties.dataTableToIterableProperties(data.asList(SaveProperties.IterableProperty.class));
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

        Given("^the platform has iterable-ception$", () -> {
            List<IterableValuedPropertyIO> iterableProperties = Arrays.asList(
                    new IterableValuedPropertyIO("a", Arrays.asList(
                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                    new ValuedPropertyIO("valued_in_a", "value_a"),
                                    new IterableValuedPropertyIO("b", Arrays.asList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("valued_in_b", "value_b"),
                                                    new IterableValuedPropertyIO("c", Arrays.asList(
                                                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                                                    new ValuedPropertyIO("valued_in_c", "value_c")
                                                            )))
                                                    ))
                                            )))
                                    )),
                                    new IterableValuedPropertyIO("d", Arrays.asList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("valued_in_d", "value_d")
                                            )))
                                    ))
                            )))
                    ))
            );
            platformBuilder.withIterableProperties(iterableProperties);
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        });

        When("^I( try to)? create this platform$", (String tryTo) -> {
            testContext.setResponseEntity(
                    platformClient.create(platformBuilder.buildInput(), getResponseType(tryTo, PlatformIO.class))
            );
        });

        Given("^an existing platform with this module in version (.+) and the property \"([^\"]*)\" valued accordingly$", (String version, String propertyName) -> {
            moduleBuilder.withVersion(version);
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            platformBuilder.incrementDeployedModuleIds();
            testContext.setResponseEntity(platformClient.create(platformBuilder.buildInput()));
            assertOK();
            platformBuilder.withProperty(propertyName, version);
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
            platformHistory.addPlatform();
        });

        When("^I( try to)? copy this platform( without instances & properties)?( using the same key)?( to a non-prod one)?$", (String tryTo, String withoutInstancesAndProperties, String usingTheSameKey, String toNonProd) -> {
            PlatformIO existingPlatform = platformBuilder.buildInput();
            String newName = isNotEmpty(usingTheSameKey) ? existingPlatform.getPlatformName() : existingPlatform.getPlatformName() + "-copy";
            PlatformBuilder newPlatform = new PlatformBuilder()
                    .withApplicationName(existingPlatform.getApplicationName())
                    .withPlatformName(newName);
            if (isNotEmpty(toNonProd)) {
                newPlatform.withIsProductionPlatform(false);
            }
            testContext.setResponseEntity(platformClient.copy(existingPlatform, newPlatform.buildInput(), isNotEmpty(withoutInstancesAndProperties), getResponseType(tryTo, PlatformIO.class)));
            platformBuilder.withPlatformName(newName);
            platformBuilder.withVersionId(1);
        });

        Then("^the platform is successfully created(?: with \"(.*)\" as path)?$", (String expectedModulePath) -> {
            assertOK();
            if (platformBuilder.getIsProductionPlatform() == null) {
                platformBuilder.withIsProductionPlatform(false);
            }
            // The returned deployed modules always have a non-empty modulePath, even if none was provided:
            if (platformBuilder.getDeployedModules().size() > 0 && isBlank(platformBuilder.getDeployedModules().get(0).getModulePath())) {
                platformBuilder.withNoModule();
                moduleBuilder.setLogicalGroup("#");
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            }
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            Assert.assertEquals(expectedPlatform, actualPlatform);
            if (isNotEmpty(expectedModulePath)) {
                assertEquals(expectedModulePath, actualPlatform.getDeployedModules().get(0).getModulePath());
            }
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), testContext.getResponseStatusCode());
            assertThat(testContext.getResponseBody(String.class), containsString(message));
        });

        Then("^the platform creation fails with an already exist error$", this::assertConflict);

        Then("^the platform copy fails with a not found error$", this::assertNotFound);

        Then("^the platform copy fails with an already exist error$", this::assertConflict);
    }
}
