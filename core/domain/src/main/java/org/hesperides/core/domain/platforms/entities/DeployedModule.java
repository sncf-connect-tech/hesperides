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
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Value
public class DeployedModule {

    public static final String DEFAULT_MODULE_PATH = "#";

    Long id;
    String name;
    String version;
    boolean isWorkingCopy;
    String modulePath;
    String propertiesPath;
    List<AbstractValuedProperty> valuedProperties;
    List<Instance> instances;
    List<String> instancesModel; // Liste des noms des propriétés des instances

    public DeployedModule(Long id, String name, String version, boolean isWorkingCopy, String modulePath, List<AbstractValuedProperty> valuedProperties, List<Instance> instances, List<String> instancesModel) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.isWorkingCopy = isWorkingCopy;
        this.modulePath = defaultIfBlank(modulePath, DEFAULT_MODULE_PATH);
        this.propertiesPath = generatePropertiesPath();
        this.valuedProperties = valuedProperties;
        this.instances = instances;
        this.instancesModel = instancesModel;
    }

    private DeployedModule(Long newId, DeployedModule other) {
        id = newId;
        name = other.name;
        version = other.version;
        isWorkingCopy = other.isWorkingCopy;
        modulePath = other.modulePath;
        propertiesPath = other.propertiesPath;
        valuedProperties = other.valuedProperties;
        instances = other.instances;
        instancesModel = other.instancesModel;
    }

    // public for testing
    public String generatePropertiesPath() {
        final Module.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
        return modulePath + "#" + moduleKey.getNamespaceWithoutPrefix();
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
                    identifiedModule = new DeployedModule(++sequence, deployedModule);
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

    public DeployedModule buildInstancesModel(List<ValuedProperty> globalProperties) {
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                modulePath,
                valuedProperties,
                instances,
                extractInstancesModel(valuedProperties, globalProperties));
    }

    private static List<String> extractInstancesModel(List<AbstractValuedProperty> moduleProperties, List<ValuedProperty> globalProperties) {
        return AbstractValuedProperty.flattenValuedProperties(moduleProperties)
                .stream()
                .filter(valuedProperty -> valuedProperty.valueIsInstanceProperty(globalProperties))
                .map(valuedProperty -> valuedProperty.extractInstancePropertyNameFromValue())
                .collect(Collectors.toList());
    }

    public DeployedModule setValuedProperties(List<AbstractValuedProperty> valuedProperties) {
        return new DeployedModule(
                id,
                name,
                version,
                isWorkingCopy,
                modulePath,
                valuedProperties,
                instances,
                instancesModel);
    }
}
