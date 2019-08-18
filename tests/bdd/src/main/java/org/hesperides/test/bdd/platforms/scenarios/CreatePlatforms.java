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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.platforms.builders.ValuedPropertyBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class CreatePlatforms extends HesperidesScenario implements En {

    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ValuedPropertyBuilder valuedPropertyBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;
    @Autowired
    private PlatformClient platformClient;

    public CreatePlatforms() {

        Given("^a platform to create" +
                "( (?:and|with) this module?)?" +
                "( (?:and|with) an instance(?: with properties))?$", (
                String withThisModule,
                String withAnInstance) -> {

            platformBuilder.reset();

            if (isNotEmpty(withAnInstance)) {
                // Quel est l'intérêt de valuedPropertyBuilder par rapport à ValuedPropertyIO
                // Quel est l'intérêt d'instanceBuilder ?
                valuedPropertyBuilder.withName("instance-property-a").withValue("instance-property-a-value");
                instanceBuilder.withValuedPropertyBuilder(valuedPropertyBuilder);
                valuedPropertyBuilder.withName("instance-property-b").withValue("instance-property-b-value");
                instanceBuilder.withValuedPropertyBuilder(valuedPropertyBuilder);
                deployedModuleBuilder.withInstanceBuilder(instanceBuilder);
            }

            if (isNotEmpty(withThisModule)) {
                deployedModuleBuilder.fromModuleBuider(moduleBuilder);
                platformBuilder.withDeployedModuleBuilder(deployedModuleBuilder);
            }
        });

        When("^I( try to)? create this platform$", (String tryTo) -> {
            platformClient.createPlatform(platformBuilder.buildInput(), tryTo);
            platformBuilder.updateDeployedModulesId();
        });

        Then("^the platform is successfully created(?: with \"(.*)\" as path)?$", (String expectedModulePath) -> {
            assertOK();
//            if (oldPlatformBuilder.getIsProductionPlatform() == null) {
//                oldPlatformBuilder.withIsProductionPlatform(false);
//            }
//            // The returned deployed modules always have a non-empty modulePath, even if none was provided:
//            if (oldPlatformBuilder.getDeployedModules().size() > 0 && isBlank(oldPlatformBuilder.getDeployedModules().get(0).getModulePath())) {
//                oldPlatformBuilder.withNoModule();
//                moduleBuilder.setLogicalGroup("#");
//                oldPlatformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath(), moduleBuilder.getLogicalGroup());
//            }
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = testContext.getResponseBody(PlatformIO.class);
            Assert.assertEquals(expectedPlatform, actualPlatform);
//            if (isNotEmpty(expectedModulePath)) {
//                assertEquals(expectedModulePath, actualPlatform.getDeployedModules().get(0).getModulePath());
//            }
        });
    }
}
