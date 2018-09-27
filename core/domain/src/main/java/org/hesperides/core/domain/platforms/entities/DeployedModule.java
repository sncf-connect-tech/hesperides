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
package org.hesperides.core.domain.platforms.entities;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Value
public class DeployedModule {

    Long id;
    String name;
    String version;
    boolean isWorkingCopy;
    String path;
    String propertiesPath;
    //String deploymentGroup
    List<Instance> instances;
    List<AbstractValuedProperty> properties;

    public DeployedModule(Long id, String name, String version, boolean isWorkingCopy, String path, List<Instance> instances, List<AbstractValuedProperty> properties) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.isWorkingCopy = isWorkingCopy;
        this.path = path;
        this.propertiesPath = generatePropertiesPath();
        this.instances = instances;
        this.properties = properties;
    }

    private DeployedModule(DeployedModule other, Long id) {
        this.id = id;
        this.name = other.name;
        this.version = other.version;
        this.isWorkingCopy = other.isWorkingCopy;
        this.path = other.path;
        this.propertiesPath = other.propertiesPath; // because id has no bearing on this
        this.instances = other.instances;
        this.properties = other.properties;
    }

    static List<DeployedModule> fillMissingIdentifiers(List<DeployedModule> deployedModules) {
        List<DeployedModule> deployedModulesWithId = Collections.emptyList();
        if (deployedModules != null) {
            deployedModulesWithId = new ArrayList<>();

            long sequence = maxId(deployedModules);
            for (DeployedModule deployedModule : deployedModules) {
                final DeployedModule identifiedModule;
                if (deployedModule.getId() == null || deployedModule.getId() < 1) {
                    // Si l'identifiant n'est pas défini, on l'initialise à la valeur maximale + 1
                    identifiedModule = new DeployedModule(deployedModule, ++sequence);
                } else {
                    identifiedModule = deployedModule;
                }
                deployedModulesWithId.add(identifiedModule);
            }
        }
        return deployedModulesWithId;
    }

    /**
     * L'identifiant des modules déployés est l'équivalent d'un identifiant auto-incrémenté d'une base de données relationnelle.
     * Une fois qu'il est défini, il ne bouge plus.
     *
     * @param deployedModules source à parcourir, ne peut être {@code null} mais peut contenir des instances de
     *                        {@code DeployedModules} dont l'identifiant vaut {@code null}
     * @return max trouvé, ou 0L
     */
    private static long maxId(List<DeployedModule> deployedModules) {
        return deployedModules.stream()
                .map(DeployedModule::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L);
    }

    private String generatePropertiesPath() {
        final Module.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
        return path + "#" + moduleKey.getNamespaceWithoutPrefix();
    }
}
