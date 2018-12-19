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
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.infrastructure.mongo.platforms.MongoPlatformRepository;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Data
@Document(collection = "platform")
@NoArgsConstructor
public class PlatformDocument {

    @Id
    private String id;
    @Indexed
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

    public void buildInstanceModelAndSave(MongoPlatformRepository platformRepository) {
        deployedModules = Optional.ofNullable(deployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .map(deployedModule -> deployedModule.buildInstanceModel(globalProperties))
                .collect(Collectors.toList());
        platformRepository.save(this);
    }

    public void fillExistingAndUpgradedModulesWithProperties(List<DeployedModuleDocument> providedDeployedModules, boolean copyPropertiesForUpgradedModules) {
        List<DeployedModule> deployedModulesProvided = DeployedModuleDocument.toDomainInstances(providedDeployedModules);
        List<DeployedModule> deployedModules = toDomainPlatform().fillExistingAndUpgradedModulesWithProperties(deployedModulesProvided, copyPropertiesForUpgradedModules);
        this.deployedModules = DeployedModuleDocument.fromDomainInstances(deployedModules);
    }
}
