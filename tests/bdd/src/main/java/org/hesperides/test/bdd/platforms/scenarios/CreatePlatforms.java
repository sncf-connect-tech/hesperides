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
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.AuthorizationCredentialsConfig;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.users.UserAuthorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    private UserAuthorities userAuthorities;

    private List<CompletableFuture<ResponseEntity>> concurrentCreations;

    @Given("^an existing( prod)? platform" +
            "(?: named \"([^\"]*)\")?" +
            "( (?:and|with) (?:this|those) modules?)?" +
            "(?: in logical group \"([^\"]*)\")?" +
            "( (?:and|with) an instance(?: named \"([^\"]*)\")?)?" +
            "( (?:and|with) valued properties)?" +
            "( (?:and|with) iterable properties)?" +
            "( (?:and|with) global properties)?" +
            "( (?:and|with) instance properties)?" +
            "( (?:and|with) filename and location values)?" +
            "( (?:and|with) global properties as instance values)?" +
            "(?: (?:and|with) an instance value named \"([^ ]+)\")?$")
    public void givenAnExistingPlatform(
            String prodPlatform,
            String platformName,
            String withThoseModule,
            String moduleLogicalGroup,
            String withAnInstance,
            String instanceName,
            String withValuedProperties,
            String withIterableProperties,
            String withGlobalProperties,
            String withInstanceProperties,
            String withFilenameAndLocationValues,
            String withGlobalPropertiesAsInstanceValues,
            String withInstanceValueNamed) {

        platformBuilder.reset();

        if (isNotEmpty(prodPlatform)) {
            platformBuilder.withIsProductionPlatform(true);
            userAuthorities.setAuthUserRole(AuthorizationCredentialsConfig.PROD_TEST_PROFILE);
        }

        if (isNotEmpty(platformName)) {
            platformBuilder.withPlatformName(platformName);
        }

        if (isNotEmpty(withAnInstance)) {
            if (isNotEmpty(instanceName)) {
                instanceBuilder.withName(instanceName);
            }
            if (isNotEmpty(withInstanceProperties)) {
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
                deployedModuleBuilder.fromModuleBuilder(moduleBuilder);
                if (isNotEmpty(moduleLogicalGroup)) {
                    deployedModuleBuilder.withModulePath("#" + moduleLogicalGroup);
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
            if (isNotEmpty(withValuedProperties)) {
                moduleBuilder.buildPropertiesModel().getProperties().forEach(property ->
                        deployedModuleBuilder.withValuedProperty(property.getName(), property.getName() + "-value"));
            }
            if (isNotEmpty(withIterableProperties)) {
                deployedModuleBuilder.withIterableProperty(new IterableValuedPropertyIO("iterable-property",
                        Collections.singletonList(new IterablePropertyItemIO("item",
                                Collections.singletonList(new ValuedPropertyIO("property-name", "property-value"))))));
            }
            if (isNotEmpty(withInstanceProperties)) {
                deployedModuleBuilder.withValuedProperty("module-property-a", "{{instance-property-a}}");
                deployedModuleBuilder.withValuedProperty("module-property-b", "{{instance-property-b}}");
            }
            if (isNotEmpty(withFilenameAndLocationValues)) {
                deployedModuleBuilder.withValuedProperty("filename", "conf");
                deployedModuleBuilder.withValuedProperty("location", "etc");
            }
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
                deployedModuleBuilder.fromModuleBuilder(moduleBuilder);
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
            deployedModuleBuilder.fromModuleBuilder(moduleBuilder);
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

        When("^I try to create this platform more than once at the same time$", () -> {
            concurrentCreations = new ArrayList<>();
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> platformClient.createPlatform(platformBuilder.buildInput(), "should-not-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> platformClient.createPlatform(platformBuilder.buildInput(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> platformClient.createPlatform(platformBuilder.buildInput(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> platformClient.createPlatform(platformBuilder.buildInput(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> platformClient.createPlatform(platformBuilder.buildInput(), "should-fail")));
        });

        Then("^only one platform creation is successful$", () -> {
            long nbFail = concurrentCreations.stream()
                    .map(CompletableFuture::join)
                    .map(ResponseEntity::getStatusCode)
                    .filter(HttpStatus::isError)
                    .count();
            Assertions.assertThat(nbFail).isGreaterThan(0);
        });

        Then("^the platform is actually created$", () -> {
            platformClient.getPlatform(platformBuilder.buildInput());
            assertOK();
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
        userAuthorities.ensureUserAuthIsSet();
        platformClient.createPlatform(platformBuilder.buildInput(), tryTo);
        if (isEmpty(tryTo)) {
            platformBuilder.incrementVersionId();
            platformBuilder.setDeployedModuleIds();
            platformHistory.addPlatformBuilder(platformBuilder);
        }
    }

    void assertPlatform() {
        assertPlatformEquals(platformBuilder);
    }

    void assertPlatformEquals(PlatformBuilder platformBuilder) {
        assertOK();
        PlatformIO expectedPlatform = platformBuilder.buildOutput();
        PlatformIO actualPlatform = testContext.getResponseBody();
        assertEquals(expectedPlatform, actualPlatform);
    }
}
