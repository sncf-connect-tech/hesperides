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

import cucumber.api.java.en.When;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
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

    @When("^I( try to)? update this platform" +
            "(, (?:adding|removing) this module)?" +
            "(?: in logical group \"([^\"]*)\")?" +
            "(, using the released version of this module)?" +
            "(, adding an instance and an instance property)?" +
            "(?:, upgrading its module to version ([^,]+))?" +
            "(, and requiring the copy of properties)?" +
            "(, with an empty payload)?" +
            "(, changing the application version)?" +
            "( to a prod one)?$")
    public void whenIupdateThisPlatform(
            String tryTo,
            String addingOrRemovingModule,
            String logicalGroup,
            String useReleasedModule,
            String addingInstanceAndInstanceProperty,
            String upgradedModuleVersion,
            String withCopy,
            String withAnEmptyPayload,
            String changeApplicationVersion,
            String toProd) {

        if (StringUtils.isNotEmpty(addingOrRemovingModule)) {
            if (addingOrRemovingModule.contains("adding")) {
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(logicalGroup), logicalGroup);
            } else {
                platformBuilder.withNoModule();
            }
        }
        if (StringUtils.isNotEmpty(useReleasedModule)) {
            platformBuilder.withNoModule();
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(logicalGroup), logicalGroup);
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
        if (StringUtils.isNotEmpty(withAnEmptyPayload)) {
            // So that "Then the platform is successfully updated" step validate there is no more modules:
            platformBuilder.withNoModule();
        }
        if (StringUtils.isNotEmpty(changeApplicationVersion)) {
            platformBuilder.withVersion("12");
        }
        if (StringUtils.isNotEmpty(toProd)) {
            platformBuilder.withIsProductionPlatform(true);
        }
        testContext.responseEntity = platformClient.update(platformBuilder.buildInput(), StringUtils.isNotEmpty(withCopy), getResponseType(tryTo, PlatformIO.class));
        platformBuilder.incrementVersionId();
    }

    public UpdatePlatforms() {
        Then("^the platform is successfully updated$", () -> {
            assertOK();
            if (this.upgradedModuleVersion != null) {
                platformBuilder.withModuleVersion(upgradedModuleVersion, true);  // we need to update the propertiesPath so that it is reflected in `expectedPlatform`:
            }
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = (PlatformIO) testContext.getResponseBody();
            assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform property model includes this instance property$", () -> {
            InstancesModelOutput model = platformClient.getInstancesModel(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
            List<String> actualPropertyNames = model.getInstanceProperties()
                    .stream()
                    .map(InstancesModelOutput.InstancePropertyOutput::getName)
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
