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
package org.hesperides.core.domain.platforms.entities;

import org.hesperides.core.domain.platforms.exceptions.DuplicateDeployedModuleIdException;
import org.junit.Test;

import java.util.Arrays;

public class PlatformTest {

    @Test
    public void testUnusedDeployedModulesIdenticalIds() {
        new Platform(null, null, true, null, Arrays.asList(
                buildDeployedModuleWithId(null),
                buildDeployedModuleWithId(null),
                buildDeployedModuleWithId(0L),
                buildDeployedModuleWithId(0L),
                buildDeployedModuleWithId(1L)
        ), null).validateDeployedModulesDistinctIds();
    }

    @Test(expected = DuplicateDeployedModuleIdException.class)
    public void testValidatingDeployedModulesIdenticalIds() {
        new Platform(null, null, true, null, Arrays.asList(
                buildDeployedModuleWithId(null),
                buildDeployedModuleWithId(1L),
                buildDeployedModuleWithId(1L)
        ), null).validateDeployedModulesDistinctIds();
    }

    private DeployedModule buildDeployedModuleWithId(Long id) {
        return new DeployedModule(id, DeployedModule.INIT_DEPLOYED_MODULE_VERSION_ID, null, null, true, null, null, null, null);
    }
}
