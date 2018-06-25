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
package org.hesperides.domain.platforms.entities;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import java.util.ArrayList;
import java.util.List;

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

    public static List<DeployedModule> getDeployedModulesWithIdAndPropertiesPath(List<DeployedModule> deployedModules) {
        List<DeployedModule> deployedModulesWithIdAndPropertiesPath = null;

        if (deployedModules != null) {
            Long maxId = getDeployedModulesMaxId(deployedModules);

            deployedModulesWithIdAndPropertiesPath = new ArrayList<>();
            for (DeployedModule deployedModule : deployedModules) {
                Long id = deployedModule.getId() == null || deployedModule.getId() < 1 ? ++maxId : deployedModule.getId();
                deployedModulesWithIdAndPropertiesPath.add(
                        new DeployedModule(
                                id,
                                deployedModule.getName(),
                                deployedModule.getVersion(),
                                deployedModule.isWorkingCopy(),
                                deployedModule.getPath(),
                                deployedModule.generatePropertiesPath(),
                                deployedModule.getInstances()
                        )
                );
            }
        }
        return deployedModulesWithIdAndPropertiesPath;
    }

    /**
     * L'identifiant des modules déployés est l'équivalent d'un identifiant auto-incrémenté d'une base de données relationnelle.
     * Une fois qu'il est défini, il ne bouge plus.
     *
     * @param deployedModules
     * @return
     */
    private static Long getDeployedModulesMaxId(List<DeployedModule> deployedModules) {
        Long maxId = 0L;
        if (deployedModules != null) {
            for (DeployedModule deployedModule : deployedModules) {
                if (deployedModule.getId() != null && deployedModule.getId() > maxId) {
                    maxId = deployedModule.getId();
                }
            }
        }
        return maxId;
    }

    public String generatePropertiesPath() {
        Module.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
        return path + "#" + moduleKey.getNamespaceWithoutPrefix();
    }
}
