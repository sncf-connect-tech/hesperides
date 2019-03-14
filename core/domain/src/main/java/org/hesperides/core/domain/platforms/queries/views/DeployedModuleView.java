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
package org.hesperides.core.domain.platforms.queries.views;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.toDomainAbstractValuedProperties;

@Value
public class DeployedModuleView {

    Long id;
    String name;
    String version;
    boolean isWorkingCopy;
    String modulePath;
    String propertiesPath;
    List<InstanceView> instances;
    List<AbstractValuedPropertyView> valuedProperties;
    List<String> instancesModel;

    public TemplateContainer.Key getModuleKey() {
        return new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
    }

    public DeployedModule toDomainDeployedModule() {
        // L'extraction des propriétés d'instance est systématiquement effectuée
        // par platformDocument.extractInstancePropertiesAndSave dans la couche infratructure.
        // On crée donc une instance de DeployedModule avec ce champ `null`.
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                modulePath,
                toDomainAbstractValuedProperties(valuedProperties),
                InstanceView.toDomainInstances(instances),
                null
        );
    }

    public static List<DeployedModule> toDomainDeployedModules(List<DeployedModuleView> deployedModuleViews) {
        return Optional.ofNullable(deployedModuleViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(DeployedModuleView::toDomainDeployedModule)
                .collect(Collectors.toList());
    }
}
