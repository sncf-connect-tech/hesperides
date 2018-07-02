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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeployedModule {

    private final Long id;
    private final String name;
    private final String version;
    private final boolean workingCopy;
    private final String path;
    private final String propertiesPath;
    //String deploymentGroup
    private final List<Instance> instances;

    public DeployedModule(Long id, String name, String version, boolean workingCopy, String path, List<Instance> instances) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.workingCopy = workingCopy;
        this.path = path;
        this.propertiesPath = generatePropertiesPath();
        this.instances = instances;
    }

    private DeployedModule(DeployedModule other, Long id) {
        this.id = id;
        this.name = other.name;
        this.version = other.version;
        this.workingCopy = other.workingCopy;
        this.path = other.path;
        this.propertiesPath = other.propertiesPath; // because id has no bearing on this
        this.instances = other.instances;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, workingCopy, path, instances);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DeployedModule) {
            DeployedModule other = (DeployedModule) obj;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(name, other.name)
                    .append(version, other.version)
                    .append(workingCopy, other.workingCopy)
                    .append(path, other.path)
                    .append(instances, other.instances)
                    .isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    static List<DeployedModule> fillMissingIdentifiers(List<DeployedModule> deployedModules) {
        if (deployedModules == null) {
            return null;
        }

        long seq = maxId(deployedModules);
        final List<DeployedModule> deployedModulesWithId = new ArrayList<>();
        for (DeployedModule mod : deployedModules) {
            final DeployedModule identified;
            if (mod.getId() == null || mod.getId() < 1) {
                // Si l'identifiant n'est pas défini, on l'initialise à la valeur maximale + 1
                identified = new DeployedModule(mod, ++seq);
            } else {
                identified = mod;
            }
            deployedModulesWithId.add(identified);
        }
        return deployedModulesWithId;
    }

    /**
     * L'identifiant des modules déployés est l'équivalent d'un identifiant auto-incrémenté d'une base de données relationnelle.
     * Une fois qu'il est défini, il ne bouge plus.
     *
     * @param in source à parcourir, ne peut être {@code null} mais peut contenir des instances de
     *           {@code DeployedModules} dont l'identifiant vaut {@code null}
     * @return max trouvé, ou 0L
     */
    private static long maxId(List<DeployedModule> in) {
        return in.stream()
                .map(DeployedModule::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L);
    }

    private String generatePropertiesPath() {
        final Module.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(workingCopy));
        return path + "#" + moduleKey.getNamespaceWithoutPrefix();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public String getPath() {
        return path;
    }

    public String getPropertiesPath() {
        return propertiesPath;
    }

    public List<Instance> getInstances() {
        return instances;
    }
}
