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
import cucumber.api.java.en.Given;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.AuthorizationCredentialsConfig;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleHistory;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.hesperides.test.bdd.platforms.OldPlatformHistory;
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
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldPlatformHistory oldPlatformHistory;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private OldModuleHistory moduleHistory;
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private UserAuthorities userAuthorities;

    @Given("^an existing( prod)? platform" +
            "(?: named \"([^\"]*)\")?" +
            "( with this module)?" +
            "( with those modules)?" +
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
                                        String withThoseModules,
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
        oldPlatformBuilder.reset();
        moduleBuilder.setLogicalGroup(logicalGroup);

        if (isNotEmpty(isProd)) {
            oldPlatformBuilder.withIsProductionPlatform(true);
            userAuthorities.setAuthUserRole(AuthorizationCredentialsConfig.PROD_TEST_PROFILE);
        }

        if (isNotEmpty(platformName)) {
            oldPlatformBuilder.withPlatformName(platformName);
        }

        if (isNotEmpty(withThisModule)) {
            if (isNotEmpty(withAnInstance)) {
                if (isNotEmpty(withGlobalPropertiesAsInstanceValues)) {
                    oldPlatformBuilder.withInstancePropertyValue("instance-module-foo", "global-module-foo");
                }
                if (isNotEmpty(withInstanceValueNamed)) {
                    oldPlatformBuilder.withInstancePropertyValue(withInstanceValueNamed, "/var");
                }
                oldPlatformBuilder.withInstance("instance-foo-1");
            }
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
        }

        if (isNotEmpty(withThoseModules)) {
            moduleHistory.getModuleBuilders().forEach(moduleBuilder -> {
                oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
                oldPlatformBuilder.incrementDeployedModuleIds();
            });
        }

        if (isNotEmpty(withTwoModulesOneWithTheSameNameAndOneWithTheSameVersion)) {
            String origName = moduleBuilder.getName();
            String origVersion = moduleBuilder.getVersion();
            moduleBuilder.withName(origName + "2");
            moduleBuilder.withVersion(origVersion);
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion + "-SNAPSHOT");
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
            // On restaure les noms & versions :
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion);
        }
        if (isNotEmpty(withTwoModulesOneWithTheSameNameAndOneWithTheSameVersion)) {
            String origName = moduleBuilder.getName();
            String origVersion = moduleBuilder.getVersion();
            moduleBuilder.withName(origName + "2");
            moduleBuilder.withVersion(origVersion);
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion + "-SNAPSHOT");
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
            // On restaure les noms & versions :
            moduleBuilder.withName(origName);
            moduleBuilder.withVersion(origVersion);
        }
        userAuthorities.ensureUserAuthIsSet();
        testContext.setResponseEntity(oldPlatformClient.create(oldPlatformBuilder.buildInput()));
        assertOK();

        if (isNotEmpty(withValuedProperties)) {
            modelBuilder.getProperties().forEach(property -> oldPlatformBuilder.withProperty(property.getName(), "12"));
            if (moduleBuilder.hasTechno()) {
                oldPlatformBuilder.withProperty("techno-foo", "12");
            }
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        }

        if (isNotEmpty(withIterableProperties)) {
            oldPlatformBuilder.withIterableProperties(Arrays.asList(
                    new IterableValuedPropertyIO("module-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-module-1", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-module-2", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-2"))))
                    )),
                    new IterableValuedPropertyIO("techno-foo", Arrays.asList(
                            new IterablePropertyItemIO("bloc-techno-1", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-1")))),
                            new IterablePropertyItemIO("bloc-techno-2", new ArrayList<>(Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-2"))))
                    ))
            ));
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        }

        if (isNotEmpty(withIterableCeption)) {
            oldPlatformBuilder.withIterableProperties(Arrays.asList(
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
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        }

        if (isNotEmpty(withGlobalProperties)) {
            oldPlatformBuilder.withGlobalProperty("global-module-foo", "12", modelBuilder);
            if (moduleBuilder.hasTechno()) {
                oldPlatformBuilder.withGlobalProperty("global-techno-foo", "12", modelBuilder);
            }
            oldPlatformBuilder.withGlobalProperty("global-filename", "abc", modelBuilder);
            oldPlatformBuilder.withGlobalProperty("global-location", "def", modelBuilder);
            oldPlatformBuilder.withGlobalProperty("unused-global-property", "12", modelBuilder);
            oldPlatformClient.saveGlobalProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(true));
            oldPlatformBuilder.incrementVersionId();
            oldPlatformBuilder.incrementGlobalPropertiesVersionId();
        }

        if (isNotEmpty(withInstanceProperties)) {
            oldPlatformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
            if (moduleBuilder.hasTechno()) {
                oldPlatformBuilder.withInstanceProperty("techno-foo", "instance-techno-foo");
            }
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        }

        if (isNotEmpty(withFilenameLocationValues)) {
            oldPlatformBuilder.withProperty("filename", "conf");
            oldPlatformBuilder.withProperty("location", "etc");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        }

        oldPlatformHistory.addPlatform();
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
                oldPlatformBuilder.withIsProductionPlatform(true);
            }

            if (isNotEmpty(platformName)) {
                oldPlatformBuilder.withPlatformName(platformName);
            } else if (isNotEmpty(sameNameDifferentLetterCase)) {
                oldPlatformBuilder.withPlatformName(oldPlatformBuilder.getPlatformName().toUpperCase());
            }

            if (isNotEmpty(platformVersion)) {
                oldPlatformBuilder.withVersion(platformVersion);
            }

            if (isNotEmpty(withThisModule)) {
                if (isNotEmpty(withAnInstance)) {

                    List<ValuedPropertyIO> instancesProperties = new ArrayList<>();
                    if (isNotEmpty(withProperties)) {
                        instancesProperties.add(new ValuedPropertyIO("instance-property-a", "instance-property-a-val"));
                        instancesProperties.add(new ValuedPropertyIO("instance-property-b", "instance-property-b-val"));
                    }

                    oldPlatformBuilder.withInstance("instance-foo-1", instancesProperties);
                }
                moduleBuilder.setLogicalGroup(withThisModule.contains("empty path") ? "" : null);
                oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            }

            if (isNotEmpty(withoutSettingIsProductionPlatform)) {
                oldPlatformBuilder.withIsProductionPlatform(null);
            }
        });

        Given("^the platform has instance properties with the same name as a global property$", () -> {
            oldPlatformBuilder.withProperty("module-foo", "{{ global-property }}");
            oldPlatformBuilder.withInstanceProperty("module-bar", "instance-property");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            oldPlatformBuilder.withGlobalProperty("global-property", "12", modelBuilder);
            oldPlatformClient.saveGlobalProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(true));
            oldPlatformBuilder.incrementVersionId();
        });

        Given("^the platform has instance properties with the same name as another module property$", () -> {
            oldPlatformBuilder.withProperty("module-foo", "{{ module-bar }}");
            oldPlatformBuilder.withProperty("module-bar", "12");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        });

        Given("^the platform has instance properties with the same name as the module property that it's declared in$", () -> {
            oldPlatformBuilder.withInstanceProperty("module-foobar", "module-foobar");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        });

        Given("^the platform has multiple instance properties declared in the same property value$", () -> {
            oldPlatformBuilder.withInstanceProperty("module-bar", "instance-property", "another-instance-property");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        });

        Given("^the platform has an instance property declared twice$", () -> {
            oldPlatformBuilder.withInstanceProperty("module-foo", "instance-property");
            oldPlatformBuilder.withInstanceProperty("module-bar", "instance-property");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        });

        Given("^the platform(?: \"([^\"]+)\")? has these valued properties$", (String platformName, DataTable data) -> {
            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, oldPlatformBuilder.getPlatformName());
            }
            List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
            valuedProperties.forEach(property -> oldPlatformBuilder.withProperty(property.getName(), property.getValue().replace("&nbsp;", " ")));
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        });

        Given("^the platform has these iterable properties$", (DataTable data) -> {
            List<IterableValuedPropertyIO> iterableProperties = SaveProperties.dataTableToIterableProperties(data.asList(SaveProperties.IterableProperty.class));
            oldPlatformBuilder.withIterableProperties(iterableProperties);
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.incrementPropertiesVersionId();
        });

        Given("^the platform(?: \"([^\"]+)\")? has these global properties$", (String platformName, DataTable data) -> {
            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, oldPlatformBuilder.getPlatformName());
            }
            List<ValuedPropertyIO> globalProperties = data.asList(ValuedPropertyIO.class);
            oldPlatformClient.saveGlobalProperties(oldPlatformBuilder.buildInput(), new PropertiesIO(0L, new HashSet<>(globalProperties), Collections.emptySet()));
            oldPlatformBuilder.incrementVersionId();
            oldPlatformBuilder.incrementGlobalPropertiesVersionId();
        });

        Given("^the platform has these instance properties$", (DataTable data) -> {
            List<ValuedPropertyIO> instanceProperties = data.asList(ValuedPropertyIO.class);
            oldPlatformBuilder.withInstance("some-instance", instanceProperties);
            oldPlatformClient.update(oldPlatformBuilder.buildInput(), false, PlatformIO.class);
            oldPlatformBuilder.incrementVersionId();
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
            oldPlatformBuilder.withIterableProperties(iterableProperties);
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        });

        When("^I( try to)? create this platform$", (String tryTo) -> {
            testContext.setResponseEntity(
                    oldPlatformClient.create(oldPlatformBuilder.buildInput(), getResponseType(tryTo, PlatformIO.class))
            );
        });

        Given("^an existing platform with this module in version (.+) and the property \"([^\"]*)\" valued accordingly$", (String version, String propertyName) -> {
            moduleBuilder.withVersion(version);
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            oldPlatformBuilder.incrementDeployedModuleIds();
            testContext.setResponseEntity(oldPlatformClient.create(oldPlatformBuilder.buildInput()));
            assertOK();
            oldPlatformBuilder.withProperty(propertyName, version);
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            oldPlatformHistory.addPlatform();
        });

        When("^I( try to)? copy this platform( without instances & properties)?( using the same key)?( to a non-prod one)?$", (String tryTo, String withoutInstancesAndProperties, String usingTheSameKey, String toNonProd) -> {
            PlatformIO existingPlatform = oldPlatformBuilder.buildInput();
            String newName = isNotEmpty(usingTheSameKey) ? existingPlatform.getPlatformName() : existingPlatform.getPlatformName() + "-copy";
            OldPlatformBuilder newPlatform = new OldPlatformBuilder()
                    .withApplicationName(existingPlatform.getApplicationName())
                    .withPlatformName(newName);
            if (isNotEmpty(toNonProd)) {
                newPlatform.withIsProductionPlatform(false);
            }
            testContext.setResponseEntity(oldPlatformClient.copy(existingPlatform, newPlatform.buildInput(), isNotEmpty(withoutInstancesAndProperties), getResponseType(tryTo, PlatformIO.class)));
            oldPlatformBuilder.withPlatformName(newName);
            oldPlatformBuilder.withVersionId(1);
        });

        Then("^the platform is successfully created(?: with \"(.*)\" as path)?$", (String expectedModulePath) -> {
            assertOK();
            if (oldPlatformBuilder.getIsProductionPlatform() == null) {
                oldPlatformBuilder.withIsProductionPlatform(false);
            }
            // The returned deployed modules always have a non-empty modulePath, even if none was provided:
            if (oldPlatformBuilder.getDeployedModules().size() > 0 && isBlank(oldPlatformBuilder.getDeployedModules().get(0).getModulePath())) {
                oldPlatformBuilder.withNoModule();
                moduleBuilder.setLogicalGroup("#");
                oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
            }
            PlatformIO expectedPlatform = oldPlatformBuilder.buildOutput();
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
