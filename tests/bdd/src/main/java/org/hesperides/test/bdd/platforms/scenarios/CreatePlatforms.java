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

import cucumber.api.java.en.Given;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class CreatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private SaveProperties saveProperties;

    @Given("^an existing platform" +
            "(?: named \"([^\"]*)\")?" +
            "( (?:and|with) (?:this|those) modules?)?" +
            "(?: in logical group \"([^\"]*)\")?" +
            "( (?:and|with) an instance)?" +
            "( (?:and|with) valued properties)?" +
            "( (?:and|with) iterable properties)?" +
            "( (?:and|with) global properties)?" +
            "( (?:and|with) instance properties)?" +
            "( (?:and|with) filename and location values)?" +
            "( (?:and|with) global properties as instance values)?" +
            "(?: (?:and|with) an instance value named \"([^ ]+)\")?$")
    public void givenAnExistingPlatform(
            String platformName,
            String withThoseModule,
            String moduleLogicalGroup,
            String withAnInstance,
            String withValuedProperties,
            String withIterableProperties,
            String withGlobalProperties,
            String withInstanceProperties,
            String withFilenameAndLocationValues,
            String withGlobalPropertiesAsInstanceValues,
            String withInstanceValueNamed) {

        platformBuilder.reset();

        if (isNotEmpty(platformName)) {
            platformBuilder.withPlatformName(platformName);
        }

        if (isNotEmpty(withAnInstance)) {
            if (isNotEmpty(withInstanceProperties)) {
                // Propriétés de module enregistrées après la création de plateforme
                deployedModuleBuilder.withValuedProperty("module-property-a", "{{instance-property-a}}");
                deployedModuleBuilder.withValuedProperty("module-property-b", "{{instance-property-b}}");
                instanceBuilder.withValuedProperty("instance-property-a", "instance-property-a-value");
                instanceBuilder.withValuedProperty("instance-property-b", "instance-property-b-value");
            }
            if (isNotEmpty(withGlobalPropertiesAsInstanceValues)) {
                instanceBuilder.withValuedProperty("instance-module-foo", "global-module-foo");
            }
            if (isNotEmpty(withInstanceValueNamed)) {
                instanceBuilder.withValuedProperty(withInstanceValueNamed, "/var");
            }
            deployedModuleBuilder.withInstanceBuilder(instanceBuilder);
        }

        if (isNotEmpty(withThoseModule)) {
            moduleHistory.getModuleBuilders().forEach(moduleBuilder -> {
                deployedModuleBuilder.fromModuleBuider(moduleBuilder);
                if (isNotEmpty(moduleLogicalGroup)) {
                    deployedModuleBuilder.withModulePath("#" + moduleLogicalGroup);
                }
                if (isNotEmpty(withValuedProperties)) {
                    deployedModuleBuilder.withValuedProperty("module-foo", "module-foo-value");
                    deployedModuleBuilder.withValuedProperty("techno-foo", "techno-foo-value");
                }
                if (isNotEmpty(withIterableProperties)) {
                    deployedModuleBuilder.withIterableProperty(new IterableValuedPropertyIO("iterable-property",
                            Collections.singletonList(new IterablePropertyItemIO("item",
                                    Collections.singletonList(new ValuedPropertyIO("property-name", "property-value"))))));
                }
                if (isNotEmpty(withFilenameAndLocationValues)) {
                    deployedModuleBuilder.withValuedProperty("filename", "conf");
                    deployedModuleBuilder.withValuedProperty("location", "etc");
                }
                platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            });
        }

        createPlatform();

        if (isNotEmpty(withGlobalProperties)) {
            platformBuilder.withGlobalProperty("global-module-foo", "global-module-foo-value");
            platformBuilder.withGlobalProperty("global-techno-foo", "global-techno-foo-value");
            platformBuilder.withGlobalProperty("global-filename", "properties.js");
            platformBuilder.withGlobalProperty("global-location", "/conf");
            platformBuilder.withGlobalProperty("unused-global-property", "12");
            saveProperties.saveGlobalProperties();
        }

        if (isNotEmpty(withValuedProperties) || isNotEmpty(withIterableProperties) || isNotEmpty(withInstanceProperties) || isNotEmpty(withFilenameAndLocationValues)) {
            saveProperties.saveValuedProperties();
        }
    }

    public CreatePlatforms() {

        Given("^a platform to create" +
                "(?: named \"([^\"]*)\")?" +
                "(?: (?:and|with) version \"([^\"]*)\")?" +
                "( (?:and|with) this module(?: with an empty path)?)?" +
                "( (?:and|with) an instance(?: with properties))?" +
                "( without setting production flag)?$", (
                String platformName,
                String platformVersion,
                String withThisModule,
                String withAnInstance,
                String withoutSettingProductionFlag) -> {

            platformBuilder.reset();

            if (isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }
            if (isNotEmpty(platformVersion)) {
                platformBuilder.withVersion(platformVersion);
            }

            if (isNotEmpty(withAnInstance)) {
                instanceBuilder.withValuedProperty("instance-property-a", "instance-property-a-value");
                instanceBuilder.withValuedProperty("instance-property-b", "instance-property-b-value");
                deployedModuleBuilder.withInstanceBuilder(instanceBuilder);
            }

            if (isNotEmpty(withThisModule)) {
                if (withThisModule.contains("with an empty path")) {
                    deployedModuleBuilder.withModulePath("");
                }
                deployedModuleBuilder.fromModuleBuider(moduleBuilder);
            }

            if (isNotEmpty(withoutSettingProductionFlag)) {
                platformBuilder.withIsProductionPlatform(null);
            }
        });

        Given("^a platform that doesn't exist$", () -> {
            platformBuilder.withPlatformName("doesn-t-exist");
        });

        Given("^an existing platform with this module in version (.+) and the property \"([^\"]*)\" valued accordingly$", (
                String moduleVersion, String propertyName) -> {

            moduleBuilder.withVersion(moduleVersion);
            deployedModuleBuilder.fromModuleBuider(moduleBuilder);
            platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            createPlatform();

            deployedModuleBuilder.withValuedProperty(propertyName, moduleVersion);
            platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        When("^I( try to)? create this platform( again)?$", (String tryTo, String again) -> {
            if (isNotEmpty(again)) {
                platformBuilder.reset();
            }
            createPlatform(tryTo);
        });

        Then("^the platform is successfully (?:created|copied)" +
                "(?: and the deployed module has the following path \"([^\"]*)\")?$", (
                String expectedModulePath) -> {

            assertPlatform();

            if (isNotEmpty(expectedModulePath)) {
                platformBuilder.getDeployedModuleBuilders().forEach(deployedModuleBuilder -> {
                    assertEquals(expectedModulePath, deployedModuleBuilder.getModulePath());
                });
            }
        });

        Then("^the platform creation fails with a conflict error$", this::assertConflict);
    }

    public void createPlatform() {
        createPlatform(null);
        assertOK();
    }

    private void createPlatform(String tryTo) {
        platformClient.createPlatform(platformBuilder.buildInput(), tryTo);
        if (isEmpty(tryTo)) {
            platformBuilder.incrementVersionId();
            platformBuilder.setDeployedModuleIds();
            platformHistory.addPlatformBuilder(platformBuilder);
        }
    }

    void assertPlatform() {
        assertOK();
        PlatformIO expectedPlatform = platformBuilder.buildOutput();
        PlatformIO actualPlatform = testContext.getResponseBody();
        assertEquals(expectedPlatform, actualPlatform);
    }
}
