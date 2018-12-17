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
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Value
public class Platform {

    Key key;
    String version;
    boolean isProductionPlatform;
    Long versionId;
    List<DeployedModule> deployedModules;
    private List<ValuedProperty> globalProperties;

    public Platform initializeVersionId() {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                1L,
                deployedModules,
                globalProperties
        );
    }

    public Platform validateVersionId(Long expectedVersionId) {
        if (!expectedVersionId.equals(versionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
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
                globalProperties
        );
    }

    @Value
    public static class Key {
        String applicationName;
        String platformName;
    }

    public List<DeployedModule> updateModules(List<DeployedModule> deployedModulesProvided, boolean copyPropertiesForUpgradedModules) {
        // cf. https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/applications/event/PlatformUpdatedCommand.java
        Map<String, DeployedModule> existingDeployedModulesPerPath = deployedModules.stream()
                .collect(toMap(DeployedModule::getPropertiesPath, identity()));

        return deployedModulesProvided.stream()
                .map(newModule -> {
                    if (existingDeployedModulesPerPath.containsKey(newModule.getPropertiesPath())) {
                        DeployedModule existingModuleWithSameId = existingDeployedModulesPerPath.get(newModule.getPropertiesPath());
                        return existingModuleWithSameId.copyWithInstances(newModule.getInstances())
                                .extractAndSetInstanceProperties(globalProperties);
                    }
                    if (!copyPropertiesForUpgradedModules) {
                        return newModule;
                    }
                    return deployedModules.stream()
                            .filter(existingModule -> newModuleMatchOldVersion(existingModule, newModule) && !newModule.getVersion().equals(existingModule.getVersion()))
                            .findAny().map(existingModule -> existingModule.copyWithVersionAndInstances(
                                    newModule.getVersion(),
                                    newModule.getInstances()
                            ).extractAndSetInstanceProperties(globalProperties))
                            .orElse(newModule);
                }).collect(Collectors.toList());
    }

    static private boolean newModuleMatchOldVersion(DeployedModule existingModule, DeployedModule newModule) {
        if (!newModule.getId().equals(existingModule.getId())) {
            return false;
        }
        // Cette comparaison permet de gérer 2 cas possible, tous les 2 valides:
        // - le `propertiesPath` du module fourni dans la payload de l'appel REST correspond à la NOUVELLE version de module
        // - le `propertiesPath` du module fourni dans la payload de l'appel REST correspond à l'ANCIENNE version de module
        // À noter qu'à ce stade nous n'avons pas accès au "vrai" `propertiesPath` fourni en entrée du controller REST.
        // Cette information est perdue lors de la conversion de DeployedModuleIO en DeployedModule a lieu dans DeployedModuleIO.toDomainInstance.
        // Ici `newModule.getPropertiesPath()` provient de DeployedModule.generatePropertiesPath() où il est reconstruit de zéro.
        String newModulePropertiesPathWithOldVersion = newModule.getPropertiesPath().replace("#" + newModule.getVersion() + "#", "#" + existingModule.getVersion() + "#");
        return newModulePropertiesPathWithOldVersion.equals(existingModule.getPropertiesPath());
    }

}
