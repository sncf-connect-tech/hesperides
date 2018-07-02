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
package org.hesperides.infrastructure.mongo.platforms.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "platform")
@NoArgsConstructor
public class PlatformDocument {

    @Id
    private PlatformKeyDocument key;
    private String version;
    private boolean productionPlatform;
    private Long versionId;
    private List<DeployedModuleDocument> deployedModules;

    public PlatformDocument(Platform platform) {
        this.key = new PlatformKeyDocument(platform.getKey());
        this.version = platform.getVersion();
        this.productionPlatform = platform.isProductionPlatform();
        this.versionId = platform.getVersionId();
        this.deployedModules = DeployedModuleDocument.fromDomainInstances(platform.getDeployedModules());

    }

    public PlatformView toPlatformView() {
        return new PlatformView(
                key.getPlatformName(),
                key.getApplicationName(),
                version,
                productionPlatform,
                DeployedModuleDocument.toDeployedModuleViews(deployedModules),
                versionId
        );
    }

    public ModulePlatformView toModulePlatformView() {
        return new ModulePlatformView(
                key.getApplicationName(),
                key.getPlatformName()
        );
    }

    public SearchPlatformView toSearchPlatformView() {
        return new SearchPlatformView(
                key.getPlatformName(),
                key.getApplicationName());
    }

    public ApplicationSearchView toApplicationSearchView() {
        return new ApplicationSearchView(key.getApplicationName());
    }
}
