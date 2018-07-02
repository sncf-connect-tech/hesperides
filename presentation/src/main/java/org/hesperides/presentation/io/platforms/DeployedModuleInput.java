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
package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.platforms.entities.DeployedModule;
import org.hesperides.presentation.io.OnlyPrintableCharacters;

import java.util.List;
import static java.util.stream.Collectors.toList;

@Value
public class DeployedModuleInput {

    Long id;

    @OnlyPrintableCharacters(subject = "deployedModules.name")
    String name;

    @OnlyPrintableCharacters(subject = "deployedModules.version")
    String version;

    @SerializedName("working_copy")
    boolean workingCopy;

    @OnlyPrintableCharacters(subject = "deployedModules.path")
    String path;

    List<InstanceIO> instances;

    public DeployedModule toDomainInstance() {
        return new DeployedModule(
                id,
                name,
                version,
                workingCopy,
                path,
                InstanceIO.toDomainInstances(instances)
        );
    }

    public static List<DeployedModule> toDomainInstances(List<DeployedModuleInput> moduleIOS) {
        List<DeployedModule> modules = null;
        if (moduleIOS != null) {
            modules = moduleIOS.stream().map(DeployedModuleInput::toDomainInstance).collect(toList());
        }
        return modules;
    }
}
