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

import cucumber.api.java8.En;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
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

    public UpdatePlatforms() {

        When("^I( try to)? update this platform" +
                "(, upgrading its module(?: to version \"(.*)\")?)?" +
                "(, adding this module(?: in logical group \"(.*)\")?)?" +
                "(, adding an instance(?: (and|with) instance properties)?)?" +
                "( and requiring the copy of properties)?$", (
                String tryTo,
                String upgradeModule,
                String moduleVersion,
                String addThisModule,
                String moduleLogicalGroup,
                String addAnInstance,
                String addInstanceProperties,
                String copyProperties) -> {

            platformBuilder.withVersion("1.1");

            if (isNotEmpty(upgradeModule)) {
                DeployedModuleBuilder existingDeployedModuleBuilder = SerializationUtils.clone(deployedModuleBuilder);
                deployedModuleBuilder.fromModuleBuider(moduleBuilder);
                if (isEmpty(copyProperties)) {
                    // S'il n'y a pas de copie des propriétés, on conserve le properties_version_id du module mis à jour
                    deployedModuleBuilder.withPropertiesVersionId(existingDeployedModuleBuilder.getPropertiesVersionId());
                }
                if (isNotEmpty(moduleVersion)) {
                    deployedModuleBuilder.withVersion(moduleVersion);
                }
                platformBuilder.replaceDeployedModuleBuilder(existingDeployedModuleBuilder, deployedModuleBuilder);
                // Ici on part du principe qu'il n'y a qu'un module déployé donc
                // on le supprime et on ajoute le nouveau. Le mieux aurait été de
                // détecté quel module a été mis à jour et de le remplacer dynamiquement.
//                platformBuilder.clearDeployedModuleBuilders();
//                platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            }

            if (isNotEmpty(addThisModule)) {
                deployedModuleBuilder.reset().fromModuleBuider(moduleBuilder);
                if (isNotEmpty(moduleLogicalGroup)) {
                    deployedModuleBuilder.withModulePath("#" + moduleLogicalGroup);
                }
                platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            }

            if (isNotEmpty(addAnInstance)) {
                if (isNotEmpty(addInstanceProperties)) {
                    // L'ajout de propriétés d'instance nécessitent qu'elles soient définies dans les valorisations
                    // au niveau du module déployé afin qu'elles soient prises en compte dans le model d'instance du module
                    platformBuilder.getDeployedModuleBuilders().get(0).withValuedProperty("module-property-a", "{{instance-property-a}}");
                    platformBuilder.getDeployedModuleBuilders().get(0).withValuedProperty("module-property-b", "{{instance-property-b}}");
                    // à bouger dans SaveProperties ?
                    platformClient.saveProperties(platformBuilder.buildInput(), deployedModuleBuilder.buildProperties(), deployedModuleBuilder.buildPropertiesPath());
                    platformBuilder.updateDeployedModuleBuilder(deployedModuleBuilder);
                    platformHistory.updatePlatformBuilder(platformBuilder);

                    instanceBuilder.withValuedProperty("instance-property-a", "instance-property-a-value");
                    instanceBuilder.withValuedProperty("instance-property-b", "instance-property-b-value");
                }
                platformBuilder.getDeployedModuleBuilders().get(0).withInstanceBuilder(instanceBuilder);
            }

            platformClient.updatePlatform(platformBuilder.buildInput(), isNotEmpty(copyProperties), tryTo);
            if (StringUtils.isEmpty(tryTo)) {
                platformHistory.updatePlatformBuilder(platformBuilder);
            }
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

//        Then("^the platform is successfully deleted", this::assertOK);
    }
}
