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
import org.hesperides.domain.platforms.entities.Instance;
import org.hesperides.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.domain.platforms.queries.views.InstanceView;
import org.hesperides.presentation.io.platforms.properties.ValorisedPropertyIO;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class DeployedModuleIO {

    Long id;
    String name;
    String version;
    @SerializedName("working_copy")
    boolean isWorkingCopy;
    @SerializedName("properties_path")
    String propertiesPath;
    String path;
    List<InstanceIO> instances;

    public static List<DeployedModule> toDomainInstances(List<DeployedModuleIO> moduleIOS) {
        List<DeployedModule> modules = null;
        if (moduleIOS != null) {
            modules = moduleIOS.stream().map(DeployedModuleIO::toDomainInstance).collect(Collectors.toList());
        }
        return modules;
    }

    public static List<DeployedModuleIO> fromDeployedModuleViews(List<DeployedModuleView> deployedModuleViews) {
        List<DeployedModuleIO> deployedModuleIOS = null;
        if (deployedModuleViews != null) {
            deployedModuleIOS = deployedModuleViews.stream().map(DeployedModuleIO::fromDeployedModuleView).collect(Collectors.toList());
        }
        return deployedModuleIOS;
    }
    public static DeployedModuleIO fromDeployedModuleView(DeployedModuleView deployedModuleView) {
        return new DeployedModuleIO(
                deployedModuleView.getId(),
                deployedModuleView.getName(),
                deployedModuleView.getVersion(),
                deployedModuleView.isWorkingCopy(),
                deployedModuleView.getPropertiesPath(),
                deployedModuleView.getPath(),
                InstanceIO.fromIstanceViews(deployedModuleView.getInstances())
        );
    }

    public DeployedModule toDomainInstance() {
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                path,
                propertiesPath,
                InstanceIO.toDomainInstances(instances)
        );
    }

    @Value
    public static class InstanceIO {

        String name;
        @SerializedName("key_values")
        List<ValorisedPropertyIO> valorisedProperties;

        public static List<Instance> toDomainInstances(List<InstanceIO> instanceIOS) {
            List<Instance> instances = null;
            if (instanceIOS != null) {
                instances = instanceIOS.stream().map(InstanceIO::toDomainInstance).collect(Collectors.toList());
            }
            return instances;
        }

        public static List<InstanceIO> fromIstanceViews(List<InstanceView> instanceViews) {
            List<InstanceIO> instanceIOS = null;
            if (instanceViews != null) {
                instanceIOS = instanceViews.stream().map(InstanceIO::fromIstanceView).collect(Collectors.toList());
            }
            return instanceIOS;
        }

        public static InstanceIO fromIstanceView(InstanceView instanceView) {
            return new InstanceIO(
                    instanceView.getName(),
                    ValorisedPropertyIO.fromPropertyViews(instanceView.getValorisedProperties())
            );
        }

        public Instance toDomainInstance() {
            return new Instance(name, ValorisedPropertyIO.toDomainInstances(valorisedProperties));
        }
    }
}
