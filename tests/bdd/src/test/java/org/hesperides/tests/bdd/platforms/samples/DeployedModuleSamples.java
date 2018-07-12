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

import org.hesperides.core.presentation.io.platforms.DeployedModuleInput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleOutput;

import java.util.Arrays;

public class DeployedModuleSamples {

    public static final String DEFAULT_MODULE_NAME = "module_name";
    public static final String DEFAULT_MODULE_VERSION = "module_version";
    public static final boolean DEFAULT_WORKING_COPY = true;
    public static final String DEFAULT_PATH = "#GROUP";
    public static final String DEFAULT_OUTPUT_PROPERTIES_PATH = "#GROUP#module_name#module_version#WORKINGCOPY";

    public static DeployedModuleInput getDeployedModuleInputWithDefaultValues(Long id) {
        return new DeployedModuleInput(
                id,
                DEFAULT_MODULE_NAME,
                DEFAULT_MODULE_VERSION,
                DEFAULT_WORKING_COPY,
                DEFAULT_PATH,
                Arrays.asList(
                        InstanceSamples.getInstanceIOWithDefaultValues(),
                        InstanceSamples.getInstanceIOWithDefaultValues()
                )
        );
    }

    public static DeployedModuleOutput getDeployedModuleOutputWithDefaultValues(Long id) {
        return new DeployedModuleOutput(
                id,
                DEFAULT_MODULE_NAME,
                DEFAULT_MODULE_VERSION,
                DEFAULT_WORKING_COPY,
                DEFAULT_OUTPUT_PROPERTIES_PATH,
                DEFAULT_PATH,
                Arrays.asList(
                        InstanceSamples.getInstanceIOWithDefaultValues(),
                        InstanceSamples.getInstanceIOWithDefaultValues()
                )
        );
    }

    public static DeployedModuleInput getDeployedModuleInputWithDefaultValues(Long id, String name, String version) {
        return new DeployedModuleInput(
                id,
                name,
                version,
                DEFAULT_WORKING_COPY,
                DEFAULT_PATH,
                Arrays.asList(
                        InstanceSamples.getInstanceIOWithDefaultValues(),
                        InstanceSamples.getInstanceIOWithDefaultValues()
                )
        );
    }
}
