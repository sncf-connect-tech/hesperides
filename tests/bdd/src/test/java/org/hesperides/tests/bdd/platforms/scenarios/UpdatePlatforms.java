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
package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.InstanceModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UpdatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;

    private String upgradedModuleVersion;

    public UpdatePlatforms() {

        When("^updating this platform(, (?:adding|removing) this module)?(, using the released version of this module)?(, adding an instance and an instance property)?(?:, upgrading its module to version ([^,]+))?(, and requiring the copy of properties)?(, with an empty payload)?$",
                (String addingOrRemovingModule, String useReleasedModule, String addingInstanceAndInstanceProperty, String upgradedModuleVersion, String withCopy, String withAnEmptyPayload) -> {
                    if (StringUtils.isNotEmpty(addingOrRemovingModule)) {
                        if (addingOrRemovingModule.contains("adding")) {
                            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath());
                        } else {
                            platformBuilder.withNoModule();
                        }
                    }
                    if (StringUtils.isNotEmpty(useReleasedModule)) {
                        moduleClient.release(moduleBuilder.build(), String.class);
                        moduleBuilder.withModuleType(ModuleIO.RELEASE);
                    }
                    if (StringUtils.isNotEmpty(upgradedModuleVersion)) {
                        this.upgradedModuleVersion = upgradedModuleVersion;
                        moduleBuilder.withVersion(upgradedModuleVersion); // nécessaire pour que lorsqu'on requête les propriétés,
                        // durant l'étape "the platform property values are also copied",
                        // le `path` de la requête inclue bien la bonne version.
                        platformBuilder.withModuleVersion(upgradedModuleVersion);
                    }
                    if (StringUtils.isNotEmpty(addingInstanceAndInstanceProperty)) {
                        platformBuilder.withInstance("instance-foo-1");
                        platformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
                        platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(false), moduleBuilder.getPropertiesPath());
                        platformBuilder.incrementVersionId();
                    }
                    PlatformIO platformInput = platformBuilder.buildInput();
                    if (StringUtils.isNotEmpty(withAnEmptyPayload)) {
                        platformInput = platformBuilder.virginWithSameVersionId().buildInput();
                        // So that "Then the platform is successfully updated" step validate there is no more modules:
                        platformBuilder.withNoModule();
                    }
                    testContext.responseEntity = platformClient.update(platformInput, StringUtils.isNotEmpty(withCopy));
                    platformBuilder.incrementVersionId();
                });

        Then("^the platform is successfully updated$", () -> {
            assertOK();
            if (this.upgradedModuleVersion != null) {
                platformBuilder.withModuleVersion(upgradedModuleVersion, true);  // we need to update the propertiesPath so that it is reflected in `expectedPlatform`:
            }
            PlatformIO expectedPlatform = platformBuilder.buildOutputWithoutIncrementingModuleIds();
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform property model includes this instance property$", () -> {
            InstanceModelOutput model = platformClient.getInstanceModel(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
            List<String> actualPropertyNames = model.getInstanceProperties()
                    .stream()
                    .map(InstanceModelOutput.InstancePropertyOutput::getName)
                    .collect(Collectors.toList());
            assertThat(actualPropertyNames, contains("instance-module-foo"));
        });

        Then("^the platform has (?:no more|zero) modules$", () -> {
            PlatformIO platform = (PlatformIO) testContext.responseEntity.getBody();
            assertThat(platform.getDeployedModules(), is(empty()));
        });

        Then("^the platform(?: still)? has (\\d+) global properties$", (Integer count) -> {
            PropertiesIO properties = platformClient.getProperties(platformBuilder.buildInput(), "#").getBody();
            assertThat(properties.getValuedProperties(), hasSize(count));
        });

        Then("^the platform has no module valued properties$", () -> {
            PropertiesIO properties = platformClient.getProperties(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
            assertThat(properties.getValuedProperties(), is(empty()));
        });
    }
}
