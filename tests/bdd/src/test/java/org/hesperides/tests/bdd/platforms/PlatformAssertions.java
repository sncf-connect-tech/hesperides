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
package org.hesperides.tests.bdd.platforms;

import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleOutput;
import org.hesperides.core.presentation.io.platforms.InstanceIO;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PlatformAssertions {

    public static void assertPlatform(PlatformOutput expectedPlatform, PlatformOutput actualPlatform) {
        assertEquals(expectedPlatform.getApplicationName(), actualPlatform.getApplicationName());
        assertEquals(expectedPlatform.getPlatformName(), actualPlatform.getPlatformName());
        assertEquals(expectedPlatform.getVersion(), actualPlatform.getVersion());
        assertEquals(expectedPlatform.getVersionId(), actualPlatform.getVersionId());
        assertDeployedModules(expectedPlatform.getDeployedModules(), actualPlatform.getDeployedModules());
    }

    public static void assertDeployedModules(List<DeployedModuleOutput> expectedDeployedModules, List<DeployedModuleOutput> actualDeployedModules) {
        assertEquals(expectedDeployedModules.size(), actualDeployedModules.size());
        for (int i = 0; i < expectedDeployedModules.size(); i++) {
            assertDeployedModule(expectedDeployedModules.get(i), actualDeployedModules.get(i));
        }
    }

    public static void assertDeployedModule(DeployedModuleOutput expectedDeployedModule, DeployedModuleOutput actualDeployedModule) {
        assertEquals(expectedDeployedModule.getId(), actualDeployedModule.getId());
        assertEquals(expectedDeployedModule.getName(), actualDeployedModule.getName());
        assertEquals(expectedDeployedModule.getVersion(), actualDeployedModule.getVersion());
        assertEquals(expectedDeployedModule.isWorkingCopy(), actualDeployedModule.isWorkingCopy());
        assertEquals(expectedDeployedModule.getPath(), actualDeployedModule.getPath());
        assertEquals(expectedDeployedModule.getPropertiesPath(), actualDeployedModule.getPropertiesPath());
        assertInstances(expectedDeployedModule.getInstances(), actualDeployedModule.getInstances());
    }

    public static void assertInstances(List<InstanceIO> expectedInstances, List<InstanceIO> actualInstances) {
        assertEquals(expectedInstances.size(), actualInstances.size());
        for (int i = 0; i < expectedInstances.size(); i++) {
            assertInstance(expectedInstances.get(i), actualInstances.get(i));
        }
    }

    public static void assertInstance(InstanceIO expectedInstance, InstanceIO actualInstance) {
        assertEquals(expectedInstance.getName(), actualInstance.getName());
        assertValuedProperties(expectedInstance.getValuedProperties(), actualInstance.getValuedProperties());
    }

    public static void assertValuedProperties(List<ValuedPropertyIO> expectedValuedProperties, List<ValuedPropertyIO> actualValuedProperties) {
        assertEquals(expectedValuedProperties.size(), actualValuedProperties.size());
        for (int i = 0; i < expectedValuedProperties.size(); i++) {
            assertValuedPropertie(expectedValuedProperties.get(i), actualValuedProperties.get(i));
        }
    }

    public static void assertValuedPropertie(ValuedPropertyIO expectedValuedProperty, ValuedPropertyIO actualValuedProperty) {
        assertEquals(expectedValuedProperty.getName(), actualValuedProperty.getName());
        assertEquals(expectedValuedProperty.getValue(), actualValuedProperty.getValue());
    }

    public static void assertApplication(ApplicationOutput expectedApplicationOutput, ApplicationOutput actualApplicationOutput) {
        assertEquals(expectedApplicationOutput.getName(), actualApplicationOutput.getName());
        assertEquals(expectedApplicationOutput.getPlatforms().size(), actualApplicationOutput.getPlatforms().size());
        for (int i = 0; i < expectedApplicationOutput.getPlatforms().size(); i++) {
            assertPlatform(expectedApplicationOutput.getPlatforms().get(i), actualApplicationOutput.getPlatforms().get(i));
        }
    }
}
