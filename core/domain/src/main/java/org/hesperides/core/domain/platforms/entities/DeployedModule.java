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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Value
public class DeployedModule {

    public static final String DEFAULT_MODULE_PATH = "#";
    public static final Long INIT_PROPERTIES_VERSION_ID = 0L;

    Long id;
    Long propertiesVersionId;
    String name;
    String version;
    boolean isWorkingCopy;
    String modulePath;
    String propertiesPath;
    List<AbstractValuedProperty> valuedProperties;
    List<Instance> instances;
    List<String> instancesModel; // Liste des noms de propriétés de toutes les instances du module

    public DeployedModule(Long id, Long propertiesVersionId, String name, String version, boolean isWorkingCopy, String modulePath, List<AbstractValuedProperty> valuedProperties, List<Instance> instances, List<String> instancesModel) {
        this.id = id;
        this.propertiesVersionId = propertiesVersionId;
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
        propertiesVersionId = other.getPropertiesVersionId() != null ? other.getPropertiesVersionId() : INIT_PROPERTIES_VERSION_ID;
        name = other.name;
        version = other.version;
        isWorkingCopy = other.isWorkingCopy;
        modulePath = other.modulePath;
        propertiesPath = other.propertiesPath;
        valuedProperties = other.valuedProperties;
        instances = other.instances;
        instancesModel = other.instancesModel;
    }

    private DeployedModule(DeployedModule other, Long newPropertiesVersionId) {
        id = other.getId();
        propertiesVersionId = newPropertiesVersionId;
        name = other.name;
        version = other.version;
        isWorkingCopy = other.isWorkingCopy;
        modulePath = other.modulePath;
        propertiesPath = other.propertiesPath;
        valuedProperties = other.valuedProperties;
        instances = other.instances;
        instancesModel = other.instancesModel;
    }

    public DeployedModule copyWithoutInstancesNorProperties() {
        return new DeployedModule(
                id,
                INIT_PROPERTIES_VERSION_ID, // Copie from scratch donc reset du deployed module version id
                name,
                version,
                isWorkingCopy,
                modulePath,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private String generatePropertiesPath() {
        Module.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
        return generatePropertiesPath(moduleKey, modulePath);
    }

    public static String generatePropertiesPath(Module.Key moduleKey, String modulePath) {
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

    static List<DeployedModule> retrieveExistingOrInitializePropertiesVersionIds(List<DeployedModule> existingDeployedModules, List<DeployedModule> newDeployedModules) {
        List<DeployedModule> deployedModulesWithPropertiesVersionIds = Collections.emptyList();
        if (newDeployedModules != null) {
            deployedModulesWithPropertiesVersionIds = new ArrayList<>();

            for (DeployedModule deployedModule : newDeployedModules) {
                final Optional<DeployedModule> existingDeployedModule = existingDeployedModules.stream().filter(
                        streamDeployedModule -> streamDeployedModule.getId().equals(deployedModule.getId())).findFirst();
                // Si le module existe déjà, on récupère systématiquement le
                // properties_version_id existant, sinon on l'initialise
                Long newPropertiesVersionId = existingDeployedModule.isPresent() ? existingDeployedModule.get().getPropertiesVersionId() : INIT_PROPERTIES_VERSION_ID;

                deployedModulesWithPropertiesVersionIds.add(new DeployedModule(deployedModule, newPropertiesVersionId));
            }
        }
        return deployedModulesWithPropertiesVersionIds;
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
                propertiesVersionId,
                name,
                version,
                isWorkingCopy,
                modulePath,
                valuedProperties,
                instances,
                extractInstancesModel(globalProperties));
    }

    private List<String> extractInstancesModel(List<ValuedProperty> globalProperties) {
        List<ValuedProperty> moduleProperties = AbstractValuedProperty.getFlatValuedProperties(this.valuedProperties);
        List<ValuedProperty> globalAndModuleProperties = Stream.concat(moduleProperties.stream(), globalProperties.stream()).collect(Collectors.toList());

        return globalAndModuleProperties
                .stream()
                .map(valuedProperty -> valuedProperty.extractInstanceProperties(globalProperties, moduleProperties))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public DeployedModule setValuedProperties(List<AbstractValuedProperty> valuedProperties) {
        return new DeployedModule(
                id,
                propertiesVersionId,
                name,
                version,
                isWorkingCopy,
                modulePath,
                valuedProperties,
                instances,
                instancesModel);
    }

    boolean isActiveModule() {
        return id != null && id > 0;
    }
}
