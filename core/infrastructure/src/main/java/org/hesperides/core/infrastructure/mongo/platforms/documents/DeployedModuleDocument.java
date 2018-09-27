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
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
public class DeployedModuleDocument {

    private Long id;
    private String name;
    private String version;
    private boolean isWorkingCopy;
    private String path;
    private String propertiesPath;
    private List<InstanceDocument> instances;
    private List<AbstractValuedPropertyDocument> valuedProperties;

    public DeployedModuleDocument(DeployedModule deployedModule) {
        this.id = deployedModule.getId();
        this.name = deployedModule.getName();
        this.version = deployedModule.getVersion();
        this.isWorkingCopy = deployedModule.isWorkingCopy();
        this.path = deployedModule.getPath();
        this.propertiesPath = deployedModule.getPropertiesPath();
        this.instances = InstanceDocument.fromDomainInstances(deployedModule.getInstances());
        this.valuedProperties = AbstractValuedPropertyDocument.fromDomainProperties(deployedModule.getProperties());
    }

    public DeployedModuleView toDeployedModuleView() {
        return new DeployedModuleView(
                id,
                name,
                version,
                isWorkingCopy,
                propertiesPath,
                path,
                InstanceDocument.toInstanceViews(instances),
                AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(valuedProperties)
        );
    }

    public static List<DeployedModuleDocument> fromDomainInstances(List<DeployedModule> deployedModules) {
        return Optional.ofNullable(deployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .map(DeployedModuleDocument::new)
                .collect(Collectors.toList());
    }

    public static List<DeployedModuleView> toDeployedModuleViews(List<DeployedModuleDocument> deployedModules) {
        return Optional.ofNullable(deployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .map(DeployedModuleDocument::toDeployedModuleView)
                .collect(Collectors.toList());
    }
}
