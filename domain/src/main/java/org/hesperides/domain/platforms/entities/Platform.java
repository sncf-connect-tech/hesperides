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
    String version;
    boolean productionPlatform;
    Long versionId;
    List<DeployedModule> deployedModules;

    public Platform initializeVersionId() {
        return new Platform(
                key,
                version,
                productionPlatform,
                1L,
                deployedModules
        );
    }

    public Platform incrementVersionId() {
        return new Platform(
                key,
                version,
                productionPlatform,
                versionId + 1,
                deployedModules
        );
    }

    public Platform updateDeployedModules() {
        return new Platform(
                key,
                version,
                productionPlatform,
                versionId,
                DeployedModule.fillMissingIdentifiers(deployedModules)
        );
    }

    @Value
    public static class Key {
        String applicationName;
        String platformName;
    }
}
