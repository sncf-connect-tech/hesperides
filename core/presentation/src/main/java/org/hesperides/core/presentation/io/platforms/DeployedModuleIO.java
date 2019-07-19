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
package org.hesperides.core.presentation.io.platforms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@AllArgsConstructor
public class DeployedModuleIO {

    Long id;
    // Pas d'annotation @NotNull afin de garantir de la rétro compatibilité
    @SerializedName("properties_version_id")
    Long propertiesVersionId;
    @OnlyPrintableCharacters(subject = "deployedModules.name")
    String name;
    @OnlyPrintableCharacters(subject = "deployedModules.version")
    String version;
    @NotNull
    @SerializedName("working_copy")
    @JsonProperty("working_copy")
    Boolean isWorkingCopy;
    @OnlyPrintableCharacters(subject = "deployedModules.path")
    @SerializedName("path")
    @JsonProperty("path")
    String modulePath;
    @SerializedName("properties_path")
    String propertiesPath;  // en tant qu'input : facultatif, inutile et toujours ignoré
    List<InstanceIO> instances;

    public DeployedModuleIO(DeployedModuleView deployedModuleView) {
        id = deployedModuleView.getId();
        propertiesVersionId = deployedModuleView.getPropertiesVersionId();
        name = deployedModuleView.getName();
        version = deployedModuleView.getVersion();
        isWorkingCopy = deployedModuleView.isWorkingCopy();
        modulePath = deployedModuleView.getModulePath();
        propertiesPath = deployedModuleView.getPropertiesPath();
        instances = InstanceIO.fromInstanceViews(deployedModuleView.getInstances());
    }

    public DeployedModule toDomainInstance() {
        // Cette classe servant à modéliser le "body" de requêtes entrantes POST & PUT /applications/{app}/platforms,
        // elle ne porte JAMAIS d'information lié aux `globalProperties` & `instanceProperties`.
        // En effet les `globalProperties` & `instanceProperties` dépendent des moustaches de templates,
        // et leur valorisation est portée par la resource /applications/{app}/platforms/{platform}/properties.
        // On crée donc une instance de DeployedModule avec ces champs `null`.
        return new DeployedModule(
                id,
                propertiesVersionId,
                name,
                version,
                isWorkingCopy,
                modulePath,
                null,
                InstanceIO.toDomainInstances(instances),
                null
        );
    }

    public static List<DeployedModule> toDomainInstances(List<DeployedModuleIO> moduleIOS) {
        return Optional.ofNullable(moduleIOS)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(DeployedModuleIO::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static List<DeployedModuleIO> fromActiveDeployedModuleViews(Stream<DeployedModuleView> activeDeployedModules) {
        return activeDeployedModules.map(DeployedModuleIO::new).collect(Collectors.toList());
    }
}
