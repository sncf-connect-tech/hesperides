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
package org.hesperides.tests.bdd.platforms.samples;

import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.PlatformInput;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;

import java.util.Arrays;

public class PlatformSamples {
    public static final String DEFAULT_PLATFORM_NAME = "TEST";
    public static final String DEFAULT_APPLICATION_NAME = "APP";
    public static final String DEFAULT_VERSION = "12";
    public static final boolean DEFAULT_PRODUCTION_PLATFORM = false;
    public static final long DEFAULT_INPUT_VERSION_ID = 0L;
    public static final long DEFAULT_OUTPUT_VERSION_ID = 1L;
    public static final String MODULE1_NAME = "module_name1";
    public static final String MODULE1_OUTPUT_PROPERTIES_PATH = "#GROUP#module_name1#module_version#WORKINGCOPY";
    public static final String MODULE2_NAME = "module_name2";
    public static final String MODULE2_OUTPUT_PROPERTIES_PATH = "#GROUP#module_name2#module_version#WORKINGCOPY";

    /*
     * Inputs
     */
    public static PlatformInput buildPlatformInputWithName(String name) {
        return new PlatformInput(
                name,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L),
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L)
                ),
                DEFAULT_INPUT_VERSION_ID
        );
    }

    public static PlatformInput buildPlatformInputWithNameAndDifferentsModules(String name) {
        return new PlatformInput(
                name,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L, MODULE1_NAME),
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L, MODULE2_NAME)
                ),
                DEFAULT_INPUT_VERSION_ID
        );
    }

    public static PlatformInput buildPlatformInputWithValues(String name) {
        return buildPlatformInputWithValues(name, DEFAULT_APPLICATION_NAME);
    }

    public static PlatformInput buildPlatformInputWithValues(String name, String application) {
        return new PlatformInput(
                name,
                application,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L),
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L)
                ),
                DEFAULT_INPUT_VERSION_ID
        );
    }

    public static PlatformInput buildPlatformInputWithExistingModule(String platformName, String moduleName, String
            moduleVersion) {
        return new PlatformInput(
                platformName,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L, moduleName, moduleVersion)
                ),
                DEFAULT_INPUT_VERSION_ID
        );
    }

    public static PlatformInput getPlatformInputWithVersionId(Long versionId) {
        return new PlatformInput(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L),
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(0L)
                ),
                versionId
        );
    }

    /*
     * Outputs
     */
    public static PlatformOutput getPlatformOutputWithDefaultValues() {
        return getPlatformOutputWithVersionId(DEFAULT_OUTPUT_VERSION_ID);
    }

    public static PlatformOutput getPlatformOutputWithVersionId(Long versionId) {
        return new PlatformOutput(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(1L),
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(2L)
                ),
                DEFAULT_PRODUCTION_PLATFORM,
                DEFAULT_VERSION,
                versionId
        );
    }

    public static ApplicationOutput getApplicationOutputWithDefaultValues() {
        return new ApplicationOutput(
                DEFAULT_APPLICATION_NAME,
                Arrays.asList(
                        getPlatformOutputWithDefaultValues()
                )
        );
    }

    public static PlatformInput buildPlatformInputWithValues2(String applicationName, String platformName) {
        return new PlatformInput(
                platformName,
                applicationName,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(),
                DEFAULT_INPUT_VERSION_ID
        );
    }
}
