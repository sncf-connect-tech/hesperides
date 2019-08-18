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

import cucumber.api.java.en.When;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.hesperides.test.bdd.platforms.OldPlatformHistory;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UpdatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldPlatformHistory oldPlatformHistory;

    public UpdatePlatforms() {
        When("^I update the module version on this platform(?: successively)? to versions? ([^a-z]+)(?: updating the value of the \"([^\"]+)\" property accordingly)?$", (String versions, String propertyName) -> {
            Arrays.stream(versions.split(", ")).forEach(version -> {
                oldPlatformBuilder.setDeployedModulesVersion(version);
                moduleBuilder.withVersion(version); // to update the properties path
                testContext.setResponseEntity(oldPlatformClient.update(oldPlatformBuilder.buildInput(), false, PlatformIO.class));
                assertOK();
                oldPlatformBuilder.incrementVersionId();
                if (StringUtils.isNotEmpty(propertyName)) {
                    oldPlatformBuilder.setProperty(propertyName, version);
                    testContext.setResponseEntity(oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath()));
                    assertOK();
                    oldPlatformBuilder.incrementVersionId();
                }
                oldPlatformHistory.addPlatform();
            });
        });

        Then("^the platform is successfully updated$", () -> {
            assertOK();
            PlatformIO expectedPlatform = oldPlatformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform property model includes this instance property$", () -> {
            InstancesModelOutput model = oldPlatformClient.getInstancesModel(oldPlatformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
            List<String> actualPropertyNames = model.getInstanceProperties()
                    .stream()
                    .map(InstancesModelOutput.InstancePropertyOutput::getName)
                    .collect(Collectors.toList());
            assertThat(actualPropertyNames, contains("instance-module-foo"));
        });

        Then("^the platform has (?:no more|zero) modules$", () -> {
            PlatformIO platform = testContext.getResponseBody(PlatformIO.class);
            assertThat(platform.getDeployedModules(), is(empty()));
        });

        Then("^the platform(?: still)? has (\\d+) global properties$", (Integer count) -> {
            PropertiesIO properties = oldPlatformClient.getProperties(oldPlatformBuilder.buildInput(), "#").getBody();
            assertThat(properties.getValuedProperties(), hasSize(count));
        });

        Then("^the platform has no module valued properties$", () -> {
            PropertiesIO properties = oldPlatformClient.getProperties(oldPlatformBuilder.buildInput(), moduleBuilder.getPropertiesPath()).getBody();
            assertThat(properties.getValuedProperties(), is(empty()));
        });

        Then("^the platform version_id is incremented twice$", () -> {
            Long expectedVersionId = oldPlatformBuilder.buildOutput().getVersionId();
            final ResponseEntity<PlatformIO> responseEntity = oldPlatformClient.get(oldPlatformBuilder.buildInput(), null, false, PlatformIO.class);
            Long actualVersionId = responseEntity.getBody().getVersionId();
            assertEquals(expectedVersionId, actualVersionId);
        });

        Then("^the platform update is rejected with a conflict error$", this::assertConflict);
    }

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
                oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
            } else {
                oldPlatformBuilder.withNoModule();
            }
        }
        if (StringUtils.isNotEmpty(upgradeModule)) {
            oldPlatformBuilder.withNoModule();
            if (StringUtils.isNotEmpty(upgradeVersion)) {
                moduleBuilder.withVersion(upgradeVersion);
            }
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
            moduleBuilder.resetPropertiesVersionId();
        }
        if (StringUtils.isNotEmpty(downgradeVersion)) {
            oldPlatformBuilder.withNoModule();
            moduleBuilder.withVersion(downgradeVersion);
            oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), logicalGroup);
            moduleBuilder.setPropertiesVersionId(2L);
        }
        if (StringUtils.isNotEmpty(addingInstanceAndInstanceProperty)) {
            oldPlatformBuilder.withInstance("instance-foo-1");
            oldPlatformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
            moduleBuilder.setPropertiesVersionId(1L);
        }
        if (StringUtils.isNotEmpty(withAnEmptyPayload)) {
            // So that "Then the platform is successfully updated" step validate there is no more modules:
            oldPlatformBuilder.withNoModule();
        }
        if (StringUtils.isNotEmpty(changePropertyValues)) {
            modelBuilder.getProperties().forEach(property -> oldPlatformBuilder.setProperty(property.getName(), "42"));
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.incrementVersionId();
        }
        if (StringUtils.isNotEmpty(changeApplicationVersion)) {
            oldPlatformBuilder.withVersion("12");
        }
        if (StringUtils.isNotEmpty(toProd)) {
            oldPlatformBuilder.withIsProductionPlatform(true);
        }
        testContext.setResponseEntity(oldPlatformClient.update(oldPlatformBuilder.buildInput(), StringUtils.isNotEmpty(withCopy), getResponseType(tryTo, PlatformIO.class)));
        oldPlatformHistory.addPlatform();
        oldPlatformBuilder.incrementVersionId();
    }
}
