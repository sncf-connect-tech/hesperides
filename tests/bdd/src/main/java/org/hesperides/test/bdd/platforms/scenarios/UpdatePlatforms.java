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
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
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

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hesperides.test.bdd.platforms.scenarios.SaveProperties.dataTableToIterableProperties;
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
    @Autowired
    private SaveProperties saveProperties;
    @Autowired
    private CreatePlatforms createPlatforms;

    @When("^I( try to)? update this platform" +
            "(?:, (?:upgrading|downgrading) its module version to \"([^\"]*)\")?" +
            "(?:, upgrading its module name to \"([^\"]*)\")?" +
            "(, upgrading its module to the release version)?" +
            "(, adding this module(?: again)?(?: in logical group \"([^\"]*)\")?)?" +
            "(, removing this module)?" +
            "(, adding an instance(?: (and|with) instance properties)?)?" +
            "(, clearing the modules)?" +
            "(, changing the platform version)?" +
            "( to a prod one)?" +
            "( and requiring the copy of properties)?$")
    public void whenIupdateThisPlatform(
            String tryTo,
            String newModuleVersion,
            String newModuleName,
            String upgradeModuleToRelease,
            String addThisModule,
            String moduleLogicalGroup,
            String removeThisModule,
            String addAnInstance,
            String addInstanceProperties,
            String clearModules,
            String changePlatformVersion,
            String toProd,
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
            } else {
                deployedModuleBuilder.setPropertiesVersionId(0L);
            }
            deployedModuleBuilder.fromModuleBuider(moduleBuilder);
            if (isNotEmpty(moduleLogicalGroup)) {
                deployedModuleBuilder.withModulePath("#" + moduleLogicalGroup);
            }
            platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
        }

        if (isNotEmpty(removeThisModule)) {
            platformBuilder.clearDeployedModuleBuilders();
        }

        if (isNotEmpty(addAnInstance)) {
            if (isNotEmpty(addInstanceProperties)) {
                // L'ajout de propriétés d'instance nécessitent qu'elles soient définies dans les valorisations
                // au niveau du module déployé afin qu'elles soient prises en compte dans le model d'instance du module
                deployedModuleBuilder.withValuedProperty("module-property-a", "{{instance-property-a}}");
                deployedModuleBuilder.withValuedProperty("module-property-b", "{{instance-property-b}}");
                saveProperties.saveValuedProperties();

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

        if (StringUtils.isNotEmpty(toProd)) {
            platformBuilder.withIsProductionPlatform(true);
        }

        platformClient.updatePlatform(platformBuilder.buildInput(), isNotEmpty(copyProperties), tryTo);

        if (StringUtils.isEmpty(tryTo)) {
            platformHistory.updatePlatformBuilder(platformBuilder);
        }
    }

    public UpdatePlatforms() {

        Given("^the platform(?: \"([^\"]+)\")? has these (valued|global|instance|iterable)? properties$", (
                String platformName, String valuedGlobalInstanceOrIterableProperties, DataTable data) -> {

            if (isNotEmpty(platformName)) {
                // On s'assure que le platformBuilder "actif" correspond bien à la plateforme explicitement nommée
                assertEquals(platformName, platformBuilder.getPlatformName());
            }

            switch (valuedGlobalInstanceOrIterableProperties) {
                case "global":
                    platformBuilder.withGlobalProperties(data.asList(ValuedPropertyIO.class));
                    saveProperties.saveGlobalProperties();

                    break;
                case "instance":
                    instanceBuilder.setValuedProperties(data.asList(ValuedPropertyIO.class));
                    saveProperties.saveInstanceProperties();

                    break;
                case "iterable":
                    deployedModuleBuilder.setIterableProperties(dataTableToIterableProperties(data));
                    saveProperties.saveValuedProperties();

                    break;
                default:
                    deployedModuleBuilder.clearValuedProperties();
                    List<ValuedPropertyIO> valuedProperties = data.asList(ValuedPropertyIO.class);
                    valuedProperties.forEach(property -> deployedModuleBuilder.withValuedProperty(property.getName(), property.getValue().replace("&nbsp;", " ")));
                    saveProperties.saveValuedProperties();
                    break;
            }
        });

        Given("^the platform has nested iterable properties$", () -> {
            List<IterableValuedPropertyIO> iterableProperties = Collections.singletonList(
                    new IterableValuedPropertyIO("a", Collections.singletonList(
                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                    new ValuedPropertyIO("valued_in_a", "value_a"),
                                    new IterableValuedPropertyIO("b", Collections.singletonList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Arrays.asList(
                                                    new ValuedPropertyIO("valued_in_b", "value_b"),
                                                    new IterableValuedPropertyIO("c", Collections.singletonList(
                                                            new IterablePropertyItemIO("", new ArrayList<>(Collections.singletonList(
                                                                    new ValuedPropertyIO("valued_in_c", "value_c")
                                                            )))
                                                    ))
                                            )))
                                    )),
                                    new IterableValuedPropertyIO("d", Collections.singletonList(
                                            new IterablePropertyItemIO("", new ArrayList<>(Collections.singletonList(
                                                    new ValuedPropertyIO("valued_in_d", "value_d")
                                            )))
                                    ))
                            )))
                    ))
            );
            deployedModuleBuilder.setIterableProperties(iterableProperties);
            saveProperties.saveValuedProperties();
        });

        When("^I update the module version on this platform(?: successively)? to versions? ([^a-z]+)" +
                "(?: updating the value of the \"(.+)\" property accordingly)?$", (
                String moduleVersions, String propertyName) -> {

            Arrays.stream(moduleVersions.split(", ")).forEach(moduleVersion -> {

                platformBuilder.getDeployedModuleBuilders().get(0).withVersion(moduleVersion);
                deployedModuleBuilder.withVersion(moduleVersion);
                platformClient.updatePlatform(platformBuilder.buildInput());
                platformHistory.updatePlatformBuilder(platformBuilder);

                if (isNotEmpty(propertyName)) {
                    deployedModuleBuilder.updateValuedProperty(propertyName, moduleVersion);
                    platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                    platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                    platformHistory.updatePlatformBuilder(platformBuilder);
                }
            });
        });

        Then("^the platform is successfully updated$", () -> createPlatforms.assertPlatform());

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
