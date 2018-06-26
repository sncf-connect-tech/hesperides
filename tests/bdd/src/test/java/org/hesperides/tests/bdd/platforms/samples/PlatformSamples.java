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

import org.hesperides.presentation.io.platforms.ApplicationOutput;
import org.hesperides.presentation.io.platforms.PlatformIO;

import java.util.Arrays;

public class PlatformSamples {
    public static final String DEFAULT_PLATFORM_NAME = "TEST";
    public static final String DEFAULT_APPLICATION_NAME = "APP";
    public static final String DEFAULT_VERSION = "12";
    public static final boolean DEFAULT_PRODUCTION_PLATFORM = false;
    public static final long DEFAULT_INPUT_VERSION_ID = 0L;
    public static final long DEFAULT_OUTPUT_VERSION_ID = 1L;

    public static PlatformIO getPlatformInputWithDefaultValues() {
        return new PlatformIO(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues(),
                        DeployedModuleSamples.getDeployedModuleInputWithDefaultValues()
                ),
                DEFAULT_INPUT_VERSION_ID
        );
    }

    public static PlatformIO getPlatformOutputWithDefaultValues() {
        return new PlatformIO(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(1L),
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(2L)
                ),
                DEFAULT_OUTPUT_VERSION_ID
        );
    }

    public static PlatformIO getPlatformInputWithVersionId(Long versionId) {
        return new PlatformIO(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(1L),
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(2L)
                ),
                versionId
        );
    }

    public static PlatformIO getPlatformOutputWithVersionId(Long versionId) {
        return new PlatformIO(
                DEFAULT_PLATFORM_NAME,
                DEFAULT_APPLICATION_NAME,
                DEFAULT_VERSION,
                DEFAULT_PRODUCTION_PLATFORM,
                Arrays.asList(
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(1L),
                        DeployedModuleSamples.getDeployedModuleOutputWithDefaultValues(2L)
                ),
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
}
