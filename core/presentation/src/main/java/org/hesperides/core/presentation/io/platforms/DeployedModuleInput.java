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

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class DeployedModuleInput {

    Long id;
    @OnlyPrintableCharacters(subject = "deployedModules.name")
    String name;
    @OnlyPrintableCharacters(subject = "deployedModules.version")
    String version;
    @SerializedName("working_copy")
    boolean isWorkingCopy;
    @OnlyPrintableCharacters(subject = "deployedModules.path")
    String path;
    List<InstanceIO> instances;

    public DeployedModule toDomainInstance() {
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                path,
                InstanceIO.toDomainInstances(instances)
        );
    }

    public static List<DeployedModule> toDomainInstances(List<DeployedModuleInput> moduleIOS) {
        return Optional.ofNullable(moduleIOS)
                .orElse(Collections.emptyList())
                .stream()
                .map(DeployedModuleInput::toDomainInstance)
                .collect(Collectors.toList());
    }
}
