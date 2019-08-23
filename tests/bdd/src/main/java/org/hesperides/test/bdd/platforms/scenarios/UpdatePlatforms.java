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
import cucumber.api.java.en.When;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class UpdatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;

    @When("^I( try to)? update this platform" +
            "(?:, (?:upgrading|downgrading) its module version to \"(.*)\")?" +
            "(?:, upgrading its module name to \"(.*)\")?" +
            "(, upgrading its module to the release version)?" +
            "(, adding this module(?: again)?(?: in logical group \"(.*)\")?)?" +
            "(, adding an instance(?: (and|with) instance properties)?)?" +
            "(, clearing the modules)?" +
            "(, changing the platform version)?" +
            "( and requiring the copy of properties)?$")
    public void whenIupdateThisPlatform(
            String tryTo,
            String newModuleVersion,
            String newModuleName,
            String upgradeModuleToRelease,
            String addThisModule,
            String moduleLogicalGroup,
            String addAnInstance,
            String addInstanceProperties,
            String clearModules,
            String changePlatformVersion,
            String copyProperties) {

        if (isNotEmpty(newModuleVersion)) {
            platformBuilder.getDeployedModuleBuilders().get(0).withVersion(newModuleVersion);
        }

        if (isNotEmpty(newModuleName)) {
            platformBuilder.getDeployedModuleBuilders().get(0).withName(newModuleName);
        }

        if (isNotEmpty(upgradeModuleToRelease)) {
            platformBuilder.getDeployedModuleBuilders().get(0).withVersionType(VersionType.RELEASE);
        }

        if (isNotEmpty(addThisModule)) {
            if (!addThisModule.contains("again")) {
                deployedModuleBuilder.reset();
            }
            deployedModuleBuilder.fromModuleBuider(moduleBuilder);
            if (isNotEmpty(moduleLogicalGroup)) {
                deployedModuleBuilder.withModulePath("#" + moduleLogicalGroup);
            }
            platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
        }

        if (isNotEmpty(addAnInstance)) {
            if (isNotEmpty(addInstanceProperties)) {
                // L'ajout de propriétés d'instance nécessitent qu'elles soient définies dans les valorisations
                // au niveau du module déployé afin qu'elles soient prises en compte dans le model d'instance du module
                deployedModuleBuilder.withValuedProperty("module-property-a", "{{instance-property-a}}");
                deployedModuleBuilder.withValuedProperty("module-property-b", "{{instance-property-b}}");
                // à bouger dans SaveProperties ?
                platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                platformHistory.updatePlatformBuilder(platformBuilder);

                instanceBuilder.withValuedProperty("instance-property-a", "instance-property-a-value");
                instanceBuilder.withValuedProperty("instance-property-b", "instance-property-b-value");
            }
            // J'utilise get(0) pour ne pas appeler dMB.withInstanceBuilder puis platformBuilder.updateDMB
            // qui incrémente le propertiesVersionId mais c'est à revoir
            platformBuilder.getDeployedModuleBuilders().get(0).withInstanceBuilder(instanceBuilder);
        }

        if (isNotEmpty(clearModules)) {
            platformBuilder.clearDeployedModuleBuilders();
        }

        if (isNotEmpty(changePlatformVersion)) {
            platformBuilder.withVersion("1.1");
        }

        platformClient.updatePlatform(platformBuilder.buildInput(), isNotEmpty(copyProperties), tryTo);

        if (StringUtils.isEmpty(tryTo)) {
            platformHistory.updatePlatformBuilder(platformBuilder);
        }
    }

    public UpdatePlatforms() {

        // à bouger dans SaveProperties ?
        Given("^the platform(?: \"([^\"]+)\")? has these valued properties$", (String platformName, DataTable data) -> {
            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, platformBuilder.getPlatformName());
            }
            deployedModuleBuilder.withValuedProperties(data.asList(ValuedPropertyIO.class));
            // à bouger dans SaveProperties ?
            platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        // à bouger dans SaveProperties ?
        Given("^the deployed module has properties with values referencing global properties$", () -> {
            deployedModuleBuilder.withValuedProperty("property-a", "{{ global-module-foo }}");
            deployedModuleBuilder.withValuedProperty("property-b", "{{ global-techno-foo }}");
            deployedModuleBuilder.withValuedProperty("property-c", "{{ global-filename }} {{ global-location }}");
            // à bouger dans SaveProperties ?
            platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
            platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
            platformHistory.updatePlatformBuilder(platformBuilder);
        });

        When("^I update the module version on this platform(?: successively)? to versions? ([^a-z]+)" +
                "(?: updating the value of the \"(.+)\" property accordingly)?$", (
                String moduleVersions, String propertyName) -> {

            Arrays.stream(moduleVersions.split(", ")).forEach(moduleVersion -> {

                platformBuilder.getDeployedModuleBuilders().get(0).withVersion(moduleVersion);
                deployedModuleBuilder.withVersion(moduleVersion);
                platformClient.updatePlatform(platformBuilder.buildInput(), false, null);
                platformHistory.updatePlatformBuilder(platformBuilder);

                if (isNotEmpty(propertyName)) {
                    deployedModuleBuilder.updateValuedProperty(propertyName, moduleVersion);
                    platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                    platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                    platformHistory.updatePlatformBuilder(platformBuilder);
                }
            });
        });

        Then("^the platform is successfully updated$", () -> {
            assertOK();
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody();
            assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^the platform instance model includes these instance properties$", () -> {
            InstancesModelOutput expectedInstanceModel = platformBuilder.buildInstanceModel();
            InstancesModelOutput actualInstanceModel = platformClient.getInstancesModel(platformBuilder.buildInput(),
                    platformBuilder.getDeployedModuleBuilders().get(0).buildPropertiesPath());
            assertEquals(expectedInstanceModel, actualInstanceModel);
        });

        Then("^property \"([^\"]*)\" has for value \"([^\"]*)\" on the platform$", (String propertyName, String expectedValue) -> {
            PropertiesIO actualProperties = platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
            Optional<ValuedPropertyIO> matchingProperty = actualProperties.getValuedProperties().stream().filter(property -> property.getName().equals(propertyName)).findFirst();
            Assertions.assertThat(matchingProperty).isPresent();
            assertEquals(expectedValue, matchingProperty.get().getValue());
        });

        Then("^property \"([^\"]*)\" has no value on the platform$", (String propertyName) -> {
            PropertiesIO actualProperties = platformClient.getProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
            Optional<ValuedPropertyIO> matchingProperty = actualProperties.getValuedProperties().stream().filter(property -> property.getName().equals(propertyName)).findFirst();
            Assertions.assertThat(matchingProperty).isNotPresent();
        });
    }
}
