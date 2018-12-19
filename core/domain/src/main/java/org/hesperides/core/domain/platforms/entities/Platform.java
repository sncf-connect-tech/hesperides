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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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


    public Platform fillDeployedModulesMissingIds() {
        return new Platform(
                key,
                version,
                isProductionPlatform,
                versionId,
                DeployedModule.fillMissingIdentifiers(deployedModules),
                globalProperties
        );
    }
    @Value
    public static class Key {
        String applicationName;
        String platformName;
    }

    /**
     * Lorsqu'on met à jour une plateforme, le valorisation au niveau de chaque module déployé n'est pas fournie.
     * Il faut donc rapatrier cette valorisation lorsqu'il s'agit d'un module existant.
     *
     * Il est possible de copier les propriétés des modules déployés dont la version a été modifiée.
     *
     * Il y a donc 3 cas possibles :
     * - Module existant
     * - Nouvelle version de module avec copie de propriétés
     * - Nouveau module
     */
    public List<DeployedModule> fillExistingAndUpgradedModulesWithProperties(List<DeployedModule> providedModules, boolean copyPropertiesForUpgradedModules) {
        // cf. https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/applications/event/PlatformUpdatedCommand.java

        // Map permettant de retrouver plus facilement un module existant à partir de son `propertiesPath`.
        // Le `propertiesPath` permet d'identifier un module dans un groupe logique.
        Map<String, DeployedModule> existingModulesPerPath = deployedModules.stream()
                .collect(Collectors.toMap(DeployedModule::getPropertiesPath, Function.identity()));

        return providedModules.stream()
                .map(providedModule -> {
                    DeployedModule deployedModule;

                    if (existingModulesPerPath.containsKey(providedModule.getPropertiesPath())) {
                        // Module existant
                        DeployedModule existingModule = existingModulesPerPath.get(providedModule.getPropertiesPath());
                        deployedModule = providedModule.setValuedProperties(existingModule.getValuedProperties());

                    } else if (copyPropertiesForUpgradedModules) {
                        // Copie de propriété pour les modules dont la version a été mise à jour
                        deployedModule = deployedModules.stream()
                                .filter(existingModule -> isModuleVersionUpdated(existingModule, providedModule))
                                .findAny()
                                .map(existingModule -> providedModule.setValuedProperties(existingModule.getValuedProperties()))
                                .orElse(providedModule); // Si une autre propriété que la version est mise à jour, on considère que c'est un nouveau module
                    } else {
                        // Nouveau module
                        deployedModule = providedModule;
                    }
                    // Il n'est pas nécessaire de régénérer le model d'instances
                    // puisque c'est fait juste avant l'enregistrement en base
                    return deployedModule;

                }).collect(Collectors.toList());
    }

    private static boolean isModuleVersionUpdated(DeployedModule existingModule, DeployedModule providedModule) {
        boolean isModuleVersionUpdated = false;
        if (providedModule.getId().equals(existingModule.getId())) {
            // Cette comparaison permet de gérer 2 cas possible, tous les 2 valides :
            // - le `propertiesPath` du module fourni dans la payload de l'appel REST correspond à la NOUVELLE version de module
            // - le `propertiesPath` du module fourni dans la payload de l'appel REST correspond à l'ANCIENNE version de module
            // À noter qu'à ce stade nous n'avons pas accès au "vrai" `propertiesPath` fourni en entrée du controller REST.
            // Cette information est perdue lors de la conversion de DeployedModuleIO en DeployedModule a lieu dans DeployedModuleIO.toDomainInstance.
            // Ici `newModule.getPropertiesPath()` provient de DeployedModule.generatePropertiesPath() où il est reconstruit de zéro.

            isModuleVersionUpdated = providedModule.getPath().equals(existingModule.getPath())
                    && providedModule.getName().equals(existingModule.getName())
                    && providedModule.isWorkingCopy() == existingModule.isWorkingCopy()
                    && !providedModule.getVersion().equals(existingModule.getVersion());
        }
        return isModuleVersionUpdated;
    }
}
