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
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UpdatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    @When("^I( try to)? update this platform" +
            "(, (?:adding|removing) this module)?" +
            "(?: in logical group \"([^\"]*)\")?" +
            "(, adding an instance and an instance property)?" +
            "(, upgrading its module(?: to version \"([^\"]*)\")?)?" +
            "(?:, downgrading its module to version \"([^\"]*)\")?" +
            "(, and requiring the copy of properties)?" +
            "(, with an empty payload)?" +
            "(, changing property values)?" +
            "(, changing the application version)?" +
            "( to a prod one)?$")
    public void whenIupdateThisPlatform(
            String tryTo,
            String addingOrRemovingModule,
            String logicalGroup,
            String addingInstanceAndInstanceProperty,
            String upgradeModule,
            String upgradeVersion,
            String downgradeVersion,
            String withCopy,
            String withAnEmptyPayload,
            String changePropertyValues,
            String changeApplicationVersion,
            String toProd) {
        moduleBuilder.setLogicalGroup(logicalGroup);
        if (StringUtils.isNotEmpty(addingOrRemovingModule)) {
            if (addingOrRemovingModule.contains("adding")) {
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
            } else {
                platformBuilder.withNoModule();
            }
        }
        if (StringUtils.isNotEmpty(upgradeModule)) {
            platformBuilder.withNoModule();
            if (StringUtils.isNotEmpty(upgradeVersion)) {
                moduleBuilder.withVersion(upgradeVersion);
            }
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
        }
        if (StringUtils.isNotEmpty(downgradeVersion)) {
            platformBuilder.withNoModule();
            moduleBuilder.withVersion(downgradeVersion);
            platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
        }
        if (StringUtils.isNotEmpty(addingInstanceAndInstanceProperty)) {
            platformBuilder.withInstance("instance-foo-1");
            platformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }
        if (StringUtils.isNotEmpty(withAnEmptyPayload)) {
            // So that "Then the platform is successfully updated" step validate there is no more modules:
            platformBuilder.withNoModule();
        }
        if (StringUtils.isNotEmpty(changePropertyValues)) {
            modelBuilder.getProperties().forEach(property -> platformBuilder.setProperty(property.getName(), "42"));
            platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            platformBuilder.incrementVersionId();
        }
        if (StringUtils.isNotEmpty(changeApplicationVersion)) {
            platformBuilder.withVersion("12");
        }
        if (StringUtils.isNotEmpty(toProd)) {
            platformBuilder.withIsProductionPlatform(true);
        }
        testContext.setResponseEntity(platformClient.update(platformBuilder.buildInput(), StringUtils.isNotEmpty(withCopy), getResponseType(tryTo, PlatformIO.class)));
        platformHistory.addPlatform();
        platformBuilder.incrementVersionId();
    }

    public UpdatePlatforms() {
        When("^I update the module version on this platform(?: successively)? to versions? ([^a-z]+)(?: updating the value of the \"([^\"]+)\" property accordingly)?$", (String versions, String propertyName) -> {
            Arrays.stream(versions.split(", ")).forEach(version -> {
                platformBuilder.setDeployedModulesVersion(version);
                moduleBuilder.withVersion(version); // to update the properties path
                testContext.setResponseEntity(platformClient.update(platformBuilder.buildInput(), false, PlatformIO.class));
                assertOK();
                platformBuilder.incrementVersionId();
                if (StringUtils.isNotEmpty(propertyName)) {
                    platformBuilder.setProperty(propertyName, version);
                    testContext.setResponseEntity(platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath()));
                    assertOK();
                    platformBuilder.incrementVersionId();
                }
                platformHistory.addPlatform();
            });
        });

        Then("^the platform is successfully updated$", () -> {
            assertOK();
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
            PlatformIO platform = (PlatformIO) testContext.getResponseBody();
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
