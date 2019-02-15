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
package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.infrastructure.mongo.platforms.MongoPlatformRepository;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.core.infrastructure.Constants.PLATFORM_COLLECTION_NAME;


@Data
@Document(collection = PLATFORM_COLLECTION_NAME)
@NoArgsConstructor
public class PlatformDocument {

    @Id
    private String id;
    private PlatformKeyDocument key;
    private String version;
    private boolean isProductionPlatform;
    private Long versionId;
    private List<DeployedModuleDocument> deployedModules;
    private List<ValuedPropertyDocument> globalProperties;

    public PlatformDocument(String id, Platform platform) {
        this.id = id;
        this.key = new PlatformKeyDocument(platform.getKey());
        this.version = platform.getVersion();
        this.isProductionPlatform = platform.isProductionPlatform();
        this.versionId = platform.getVersionId();
        this.deployedModules = DeployedModuleDocument.fromDomainInstances(platform.getDeployedModules());
        this.globalProperties = ValuedPropertyDocument.fromDomainInstances(platform.getGlobalProperties());
    }

    public PlatformView toPlatformView() {
        return new PlatformView(
                id,
                key.getPlatformName(),
                key.getApplicationName(),
                version,
                isProductionPlatform,
                DeployedModuleDocument.toDeployedModuleViews(deployedModules),
                versionId,
                ValuedPropertyDocument.toValuedPropertyViews(globalProperties)
        );
    }

    public ModulePlatformView toModulePlatformView() {
        return new ModulePlatformView(
                key.getApplicationName(),
                key.getPlatformName()
        );
    }

    public Platform toDomainPlatform() {
        return new Platform(
                key.toDomainPlatformKey(),
                version,
                isProductionPlatform,
                versionId,
                DeployedModuleDocument.toDomainInstances(deployedModules),
                ValuedPropertyDocument.toDomainInstances(globalProperties)
        );
    }

    public SearchApplicationResultView toSearchApplicationResultView() {
        return new SearchApplicationResultView(key.getApplicationName());
    }

    public SearchPlatformResultView toSearchPlatformResultView() {
        return new SearchPlatformResultView(key.getPlatformName());
    }

    public static ApplicationView toApplicationView(String applicationName, List<PlatformDocument> platformDocuments) {
        return new ApplicationView(
                applicationName,
                platformDocuments
                        .stream()
                        .map(PlatformDocument::toPlatformView)
                        .collect(Collectors.toList())
        );
    }

    public void buildInstancesModelAndSave(MongoPlatformRepository platformRepository) {
        deployedModules = Optional.ofNullable(deployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .map(deployedModule -> deployedModule.buildInstancesModel(globalProperties))
                .collect(Collectors.toList());
        platformRepository.save(this);
    }

    /**
     * 4 cas d'utilisation :
     * - Nouveau module : on l'ajoute simplement
     * - Module existant (id + path) : on copie les propriétés existantes pour ne pas les perdre
     * - Mise à jour (id existant mais nouveau path) : on passe l'identifiant du module existant à null
     * et on récupère les propriétés du module existant si c'est demandé => `copyPropertiesForUpgradedModules`
     * - Retour arrière (id et path existants mais sur 2 modules distincts) : on récupère les propriétés
     * du path existant et on met l'id du module existant à 0
     */
    public void updateModules(List<DeployedModuleDocument> providedModules, boolean copyPropertiesForUpgradedModules, Long versionId) {
        List<DeployedModuleDocument> newModuleList = new ArrayList<>();

        providedModules.forEach(providedModule -> {
            Optional<DeployedModuleDocument> existingModuleByIdAndPath = getModuleByIdAndPath(providedModule);
            if (existingModuleByIdAndPath.isPresent()) {
                providedModule.setValuedProperties(existingModuleByIdAndPath.get().getValuedProperties());
            } else {
                Optional<DeployedModuleDocument> existingModuleById = getModuleById(providedModule.getId());
                Optional<DeployedModuleDocument> existingModuleByPath = getModuleByPath(providedModule.getPropertiesPath());
                if (existingModuleById.isPresent()) {
                    existingModuleById.get().setId(0L);
                    newModuleList.add(existingModuleById.get());
                    if (copyPropertiesForUpgradedModules) {
                        providedModule.setValuedProperties(existingModuleById.get().getValuedProperties());
                    } else if (existingModuleByPath.isPresent()) {
                        providedModule.setValuedProperties(existingModuleByPath.get().getValuedProperties());
                    }
                } else if (existingModuleByPath.isPresent()) {
                    // Cas de retour arrière après suppression
                    providedModule.setValuedProperties(existingModuleByPath.get().getValuedProperties());
                }
            }
            newModuleList.add(providedModule);
        });
        // Supprimer l'identifiant des modules qui n'ont pas été fournis (pour conserver les valorisations)
        deployedModules.forEach(existingModule -> {
            if (existingModule.hasBeenRemovedFrom(newModuleList)) {
                existingModule.setId(0L);
                newModuleList.add(existingModule);
            }
        });

        deployedModules = newModuleList;
    }

    private Optional<DeployedModuleDocument> getModuleByIdAndPath(DeployedModuleDocument providedModule) {
        return deployedModules.stream().filter(existingModule -> providedModule.getId().equals(existingModule.getId()) &&
                providedModule.getPropertiesPath().equals(existingModule.getPropertiesPath())).findFirst();
    }

    private Optional<DeployedModuleDocument> getModuleById(Long providedModuleId) {
        return deployedModules.stream().filter(existingModule -> providedModuleId.equals(existingModule.getId())).findFirst();
    }

    private Optional<DeployedModuleDocument> getModuleByPath(String providedModulePath) {
        return deployedModules.stream().filter(existingModule -> providedModulePath.equals(existingModule.getPropertiesPath())).findFirst();
    }
}
