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
package org.hesperides.domain.platforms.entities;

import lombok.Value;

import java.util.List;

@Value
public class Platform {

    Key key;
    boolean productionPlatform;
    Long versionId;
    List<DeployedModule> deployedModules;

    public Platform initVersionId() {
        return new Platform(
                key,
                productionPlatform,
                1L,
                deployedModules
        );
    }

    public Platform setNewDeployedModulesId() {
        return new Platform(
                key,
                productionPlatform,
                versionId,
                DeployedModule.setNewDeployedModulesId(deployedModules)
        );
    }

    public Platform setDeployedModulesPropertiesPath() {
        return new Platform(
                key,
                productionPlatform,
                versionId,
                DeployedModule.setDeployedModulesPropertiesPath(deployedModules)
        );
    }

    @Value
    public static class Key {
        String applicationName;
        String platformName;
        String version;
    }
}
