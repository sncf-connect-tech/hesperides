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
import org.hesperides.domain.platforms.entities.DeployedModule;
import org.hesperides.domain.platforms.queries.views.DeployedModuleView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class DeployedModuleDocument {

    private Long id;
    private String name;
    private String version;
    private boolean isWorkingCopy;
    private String path;
    private String propertiesPath;
    private List<InstanceDocument> instances;

    public static List<DeployedModuleDocument> fromDomainInstances(List<DeployedModule> deployedModules) {
        List<DeployedModuleDocument> deployedModuleDocuments = null;
        if (deployedModules != null) {
            deployedModuleDocuments = deployedModules.stream().map(DeployedModuleDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return deployedModuleDocuments;
    }

    public static DeployedModuleDocument fromDomainInstance(DeployedModule deployedModule) {
        DeployedModuleDocument deployedModuleDocument = new DeployedModuleDocument();
        deployedModuleDocument.setId(deployedModule.getId());
        deployedModuleDocument.setName(deployedModule.getName());
        deployedModuleDocument.setVersion(deployedModule.getVersion());
        deployedModuleDocument.setWorkingCopy(deployedModule.isWorkingCopy());
        deployedModuleDocument.setPath(deployedModule.getPath());
        deployedModuleDocument.setPropertiesPath(deployedModule.getPropertiesPath());
        deployedModuleDocument.setInstances(InstanceDocument.fromDomainInstances(deployedModule.getInstances()));
        return deployedModuleDocument;
    }

    public static List<DeployedModuleView> toDeployedModuleViews(List<DeployedModuleDocument> deployedModules) {
        List<DeployedModuleView> deployedModuleViews = null;
        if (deployedModules != null) {
            deployedModuleViews = deployedModules.stream().map(DeployedModuleDocument::toDeployedModuleView).collect(Collectors.toList());
        }
        return deployedModuleViews;
    }

    public DeployedModuleView toDeployedModuleView() {
        return new DeployedModuleView(
                id,
                name,
                version,
                isWorkingCopy,
                propertiesPath,
                path,
                InstanceDocument.toInstanceViews(instances)
        );
    }
}
