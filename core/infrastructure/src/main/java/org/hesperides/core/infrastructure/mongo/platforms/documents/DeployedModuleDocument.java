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
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
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
    private Long propertiesVersionId;
    private String name;
    private String version;
    private boolean isWorkingCopy;
    private String modulePath;
    private String propertiesPath;
    private List<AbstractValuedPropertyDocument> valuedProperties;
    private List<InstanceDocument> instances;
    private List<String> instancesModel; // Noms des propriétés des instances

    public DeployedModuleDocument(DeployedModule deployedModule) {
        id = deployedModule.getId();
        propertiesVersionId = deployedModule.getPropertiesVersionId();
        name = deployedModule.getName();
        version = deployedModule.getVersion();
        isWorkingCopy = deployedModule.isWorkingCopy();
        modulePath = deployedModule.getModulePath();
        propertiesPath = deployedModule.getPropertiesPath();
        valuedProperties = AbstractValuedPropertyDocument.fromAbstractDomainInstances(deployedModule.getValuedProperties());
        instances = InstanceDocument.fromDomainInstances(deployedModule.getInstances());
        instancesModel = deployedModule.getInstancesModel();
    }

    public DeployedModuleView toDeployedModuleView() {
        return new DeployedModuleView(
                id,
                propertiesVersionId,
                name,
                version,
                isWorkingCopy,
                modulePath,
                propertiesPath,
                InstanceDocument.toInstanceViews(instances),
                AbstractValuedPropertyDocument.toViews(valuedProperties),
                instancesModel
        );
    }

    public static List<DeployedModuleDocument> fromDomainInstances(List<DeployedModule> deployedModules) {
        return Optional.ofNullable(deployedModules)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(DeployedModuleDocument::new)
                .collect(Collectors.toList());
    }

    public static List<DeployedModuleView> toDeployedModuleViews(List<DeployedModuleDocument> deployedModules) {
        return Optional.ofNullable(deployedModules)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(DeployedModuleDocument::toDeployedModuleView)
                .collect(Collectors.toList());
    }

    public DeployedModule toDomainInstance() {
        return new DeployedModule(id, propertiesVersionId, name, version, isWorkingCopy, modulePath,
                AbstractValuedPropertyDocument.toAbstractDomainInstances(valuedProperties),
                InstanceDocument.toDomainInstances(instances),
                instancesModel);
    }

    public static List<DeployedModule> toDomainInstances(List<DeployedModuleDocument> modules) {
        return modules
                .stream()
                .map(DeployedModuleDocument::toDomainInstance)
                .collect(Collectors.toList());
    }

    public DeployedModuleDocument buildInstancesModel(List<ValuedPropertyDocument> globalPropertyDocuments) {
        List<ValuedProperty> globalProperties = ValuedPropertyDocument.toDomainInstances(globalPropertyDocuments);
        DeployedModule deployedModuleWithInstanceProperties = this.toDomainInstance().buildInstancesModel(globalProperties);
        return new DeployedModuleDocument(deployedModuleWithInstanceProperties);
    }

    public boolean hasBeenRemovedFrom(List<DeployedModuleDocument> newModuleList) {
        return newModuleList.stream()
                .noneMatch(newModule -> propertiesPath.equals(newModule.getPropertiesPath()));
    }
}
