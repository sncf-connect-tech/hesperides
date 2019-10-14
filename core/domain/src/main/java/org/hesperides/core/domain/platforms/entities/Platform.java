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

import lombok.Value;
import org.axonframework.common.digest.Digester;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.DuplicateDeployedModuleIdException;

import java.util.List;

@Value
public class Platform {

    public static final String GLOBAL_PROPERTIES_PATH = "#";

    Key key;
    String version;
    boolean isProductionPlatform;
    Long versionId;
    List<DeployedModule> deployedModules;
    Long globalPropertiesVersionId;
    List<ValuedProperty> globalProperties;

    public static boolean isGlobalPropertiesPath(String propertiesPath) {
        return GLOBAL_PROPERTIES_PATH.equals(propertiesPath);
    }

    public Key getKey() { // Doit être explicite car employé dans Platform.kt
        return key;
    }

    public Platform initializeVersionId() {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                1L,
                deployedModules,
                globalPropertiesVersionId,
                globalProperties
        );
    }

    public Platform validateVersionId(Long expectedVersionId) {
        if (!expectedVersionId.equals(versionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
        }
        return this;
    }

    public Platform validateDeployedModulesDistinctIds() {
        if (deployedModules != null) {
            long nbIds = deployedModules.stream().filter(DeployedModule::isActiveModule).map(DeployedModule::getId).count();
            long nbDistinctIds = deployedModules.stream().filter(DeployedModule::isActiveModule).map(DeployedModule::getId).distinct().count();
            if (nbIds != nbDistinctIds) {
                throw new DuplicateDeployedModuleIdException();
            }
        }
        return this;
    }

    public Platform incrementVersionId() {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                versionId + 1,
                deployedModules,
                globalPropertiesVersionId,
                globalProperties
        );
    }

    public Platform fillDeployedModulesMissingIds() {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                versionId,
                DeployedModule.fillMissingIdentifiers(deployedModules),
                globalPropertiesVersionId,
                globalProperties
        );
    }

    public Platform retrieveExistingOrInitializePropertiesVersionIds(List<DeployedModule> existingDeployedModules) {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                versionId,
                DeployedModule.retrieveExistingOrInitializePropertiesVersionIds(existingDeployedModules, deployedModules),
                globalPropertiesVersionId,
                globalProperties
        );
    }

    @Value
    public static class Key {
        String applicationName;
        String platformName;

        public String toString() {
            return applicationName + "-" + platformName;
        }

        public String generateHash() {
            return Digester.md5Hex(toString());
        }
    }
}
