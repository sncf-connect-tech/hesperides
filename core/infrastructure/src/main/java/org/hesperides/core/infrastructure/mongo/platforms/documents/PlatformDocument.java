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
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformPropertiesView;
import org.hesperides.core.domain.platforms.queries.views.properties.PropertySearchResultView;
import org.hesperides.core.infrastructure.MinimalPlatformRepository;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hesperides.core.infrastructure.mongo.Collections.PLATFORM;


@Data
@Document(collection = PLATFORM)
@NoArgsConstructor
public class PlatformDocument {

    private static int DEFAULT_NUMBER_OF_ARCHIVED_MODULE_VERSIONS = 2;

    @Id
    private String id;
    private PlatformKeyDocument key;
    private String version;
    private boolean isProductionPlatform;
    private Long versionId;
    private List<DeployedModuleDocument> deployedModules;
    private Long globalPropertiesVersionId;
    private List<ValuedPropertyDocument> globalProperties;

    public PlatformDocument(String id, Platform platform) {
        this.id = id;
        this.key = new PlatformKeyDocument(platform.getKey());
        this.version = platform.getVersion();
        this.isProductionPlatform = platform.isProductionPlatform();
        this.versionId = platform.getVersionId();
        this.deployedModules = DeployedModuleDocument.fromDomainInstances(platform.getDeployedModules());
        this.globalPropertiesVersionId = platform.getGlobalPropertiesVersionId();
        this.globalProperties = ValuedPropertyDocument.fromDomainInstances(platform.getGlobalProperties());
    }

    public Stream<DeployedModuleDocument> getActiveDeployedModules() {
        return Optional.ofNullable(deployedModules).orElseGet(Collections::emptyList)
                .stream().filter(deployedModule -> deployedModule.getId() > 0);
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
                globalPropertiesVersionId,
                ValuedPropertyDocument.toValuedPropertyViews(globalProperties)
        );
    }

    public ModulePlatformView toModulePlatformView() {
        return new ModulePlatformView(
                key.getApplicationName(),
                key.getPlatformName()
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
                        .collect(toList())
        );
    }

    public void buildInstancesModelAndSave(MinimalPlatformRepository platformRepository) {
        deployedModules = Optional.ofNullable(deployedModules)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(deployedModule -> deployedModule.buildInstancesModel(globalProperties))
                .collect(toList());
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
    public void updateModules(List<DeployedModuleDocument> providedModules,
                              boolean copyPropertiesForUpgradedModules,
                              int numberOfArchivedModuleVersions) {
        if (numberOfArchivedModuleVersions < DEFAULT_NUMBER_OF_ARCHIVED_MODULE_VERSIONS) {
            numberOfArchivedModuleVersions = DEFAULT_NUMBER_OF_ARCHIVED_MODULE_VERSIONS;
        }
        int finalNumberOfArchivedModuleVersions = numberOfArchivedModuleVersions;

        List<DeployedModuleDocument> newDeployedModules = new ArrayList<>();
        providedModules.forEach(providedModule -> {
            Optional<DeployedModuleDocument> existingModuleByIdAndPath = getModuleByIdAndPath(providedModule);
            if (existingModuleByIdAndPath.isPresent()) {
                // Le module n'est pas modifié donc on récupère ses propriétés
                providedModule.setValuedProperties(existingModuleByIdAndPath.get().getValuedProperties());
            } else {
                Optional<DeployedModuleDocument> existingModuleById = getModuleById(providedModule.getId());
                Optional<DeployedModuleDocument> existingModuleByPath = getModuleByPath(providedModule.getPropertiesPath());
                if (existingModuleById.isPresent() && copyPropertiesForUpgradedModules) {
                    // Mise à jour de module avec copie de propriétés
                    if (!CollectionUtils.isEmpty(existingModuleById.get().getValuedProperties())) {
                        // On vérifie que la liste de propriétés valorisées n'est pas vide
                        providedModule.setValuedProperties(existingModuleById.get().getValuedProperties());
                    } else if (existingModuleByPath.isPresent()) {
                        // Si elle est vide on récupère les propriétés d'une version archivée du module si elle existe
                        providedModule.setValuedProperties(existingModuleByPath.get().getValuedProperties());
                    }
                } else if (existingModuleByPath.isPresent()) {
                    // Retour vers une ancienne version de module précédement déployée
                    providedModule.setValuedProperties(existingModuleByPath.get().getValuedProperties());
                }
            }
            newDeployedModules.add(providedModule);
        });

        List<DeployedModuleDocument> remainingDeployedModules = getArchivedModules(finalNumberOfArchivedModuleVersions, newDeployedModules);
        // On ajoute les nouveaux modules dans un 2e temps pour préserver l'ordre d'insertion
        remainingDeployedModules.addAll(newDeployedModules);

        deployedModules = remainingDeployedModules;
    }

    /**
     * Récupère la liste des modules archivés
     */
    private List<DeployedModuleDocument> getArchivedModules(int finalNumberOfArchivedModuleVersions, List<DeployedModuleDocument> newDeployedModules) {
        // Supprimer l'identifiant des modules qui n'ont pas été fournis pour conserver
        // les valorisations, tout en limitant cette historisation pour ne pas dépasser
        // la taille max autorisée par MongoDB (16Mo par document)
        List<DeployedModuleDocument> remainingDeployedModules = deployedModules.stream()
                .filter(existingModule -> existingModule.hasBeenRemovedFrom(newDeployedModules))
                .peek(existingModule -> existingModule.setId(0L))
                .collect(toList());
        Map<String, List<DeployedModuleDocument>> inactiveDeployedModulesPerModulePathAndName = remainingDeployedModules.stream()
                .filter(deployedModule -> deployedModule.getId() == 0)
                .collect(groupingBy(deployedModule -> deployedModule.getModulePath() + "#" + deployedModule.getName()));
        inactiveDeployedModulesPerModulePathAndName.values().forEach(inactiveDeployedModules -> {
            // On supprime les plus anciens modules déployés inactifs pour n'en conserver que `finalNumberOfArchivedModuleVersions`
            if (inactiveDeployedModules.size() > finalNumberOfArchivedModuleVersions) {
                // Attention: un prérequis au bon fonctionnement de cette logique est que l'ordre d'insertion des modules déployés soit préservé
                inactiveDeployedModules.subList(0, inactiveDeployedModules.size() - finalNumberOfArchivedModuleVersions)
                        .forEach(remainingDeployedModules::remove);
            }
        });
        return remainingDeployedModules;
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

    public PlatformPropertiesView toPlatformPropertiesView() {
        List<PlatformPropertiesView.DeployedModule> deployedModules = this.deployedModules.stream()
                .map(deployedModule -> {
                    List<PlatformPropertiesView.Property> properties = deployedModule.getValuedProperties().stream()
                            .filter(ValuedPropertyDocument.class::isInstance)
                            .map(ValuedPropertyDocument.class::cast)
                            .map(valuedProperty -> new PlatformPropertiesView.Property(valuedProperty.getName(), valuedProperty.getValue()))
                            .collect(toList());

                    return new PlatformPropertiesView.DeployedModule(
                            deployedModule.getPropertiesPath(),
                            deployedModule.getId() == 0,
                            properties);

                }).collect(toList());

        return new PlatformPropertiesView(key.getApplicationName(), key.getPlatformName(), isProductionPlatform, deployedModules);
    }

    public List<PropertySearchResultView> filterToPropertySearchResultViews(String propertyName, String propertyValue) {
        return deployedModules.stream()
                .filter(deployedModule -> deployedModule.getId() != null && deployedModule.getId() > 0)
                .map(deployedModule -> deployedModule.getValuedProperties()
                        .stream()
                        .filter(ValuedPropertyDocument.class::isInstance)
                        .map(ValuedPropertyDocument.class::cast)
                        .filter(property -> property.getName().contains(propertyName) && property.getValue().contains(propertyValue))
                        .map(property -> new PropertySearchResultView(
                                property.getName(),
                                property.getValue(),
                                getKey().getApplicationName(),
                                getKey().getPlatformName(),
                                isProductionPlatform(),
                                deployedModule.getPropertiesPath())))
                .flatMap(Stream::distinct)
                .collect(toList());
    }
}
