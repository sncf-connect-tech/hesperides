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

@Value
public class DeployedModuleView {

    Long id;
    String name;
    String version;
    boolean isWorkingCopy;
    String path;
    String propertiesPath;
    List<InstanceView> instances;
    List<AbstractValuedPropertyView> valuedProperties;

    public TemplateContainer.Key getModuleKey() {
        return new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
    }

    public DeployedModule toDomainDeployedModule() {
        // Cette classe servant à modéliser une nouvelle plateforme crée via POST /applications/{app}/platforms?from_application=...&from_platform=...,
        // elle ne porte JAMAIS d'information lié aux `valuedProperties` & `instanceProperties`.
        // En effet les `valuedProperties` & `instanceProperties` dépendent des moustaches de templates,
        // et leur valorisation est portée par la resource /applications/{app}/platforms/{platform}/properties.
        // On crée donc une instance de DeployedModule avec ces champs `null`.
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                path,
                null,
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
