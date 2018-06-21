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

import org.hesperides.presentation.io.platforms.DeployedModuleIO;

import java.util.Arrays;

public class DeployedModuleSamples {

    public static final long DEFAULT_INPUT_ID = 0L;
    public static final String DEFAULT_MODULE_NAME = "module_name";
    public static final String DEFAULT_MODULE_VERSION = "module_version";
    public static final boolean DEFAULT_WORKING_COPY = true;
    public static final String DEFAULT_PATH = "#GROUP";
    public static final String DEFAULT_INPUT_PROPERTIES_PATH = null;
    public static final String DEFAULT_OUTPUT_PROPERTIES_PATH = "#GROUP#module_name#module_version#WORKINGCOPY";

    public static DeployedModuleIO getDeployedModuleInputWithDefaultValues() {
        return new DeployedModuleIO(
                DEFAULT_INPUT_ID,
                DEFAULT_MODULE_NAME,
                DEFAULT_MODULE_VERSION,
                DEFAULT_WORKING_COPY,
                DEFAULT_INPUT_PROPERTIES_PATH,
                DEFAULT_PATH,
                Arrays.asList(
                        InstanceSamples.getInstanceIOWithDefaultValues(),
                        InstanceSamples.getInstanceIOWithDefaultValues()
                )
        );
    }

    public static DeployedModuleIO getDeployedModuleOutputWithDefaultValues(Long id) {
        return new DeployedModuleIO(
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
}
