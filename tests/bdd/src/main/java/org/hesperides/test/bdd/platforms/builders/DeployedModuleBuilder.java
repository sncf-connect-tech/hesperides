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
package org.hesperides.test.bdd.platforms.builders;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DeployedModuleBuilder implements Serializable {

    @Getter
    @Setter
    private Long id;
    @Getter
    private Long propertiesVersionId;
    private String name;
    private String version;
    private String versionType;
    @Getter
    private String modulePath;
    @Getter
    private List<ValuedPropertyIO> valuedProperties;
    private List<IterableValuedPropertyIO> iterableProperties;
    @Getter
    private List<InstanceBuilder> instanceBuilders;

    public DeployedModuleBuilder() {
        reset();
    }

    public DeployedModuleBuilder reset() {
        id = null;
        propertiesVersionId = 0L;
        modulePath = "#ABC#DEF";
        valuedProperties = new ArrayList<>();
        iterableProperties = new ArrayList<>();
        instanceBuilders = new ArrayList<>();
        return this;
    }

    public void withName(String name) {
        this.name = name;
    }

    public void withVersion(String version) {
        this.version = version;
    }

    public void withVersionType(String versionType) {
        this.versionType = versionType;
    }

    public void withModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public void withInstanceBuilder(InstanceBuilder instanceBuilder) {
        instanceBuilders.add(SerializationUtils.clone(instanceBuilder));
    }

    public void withValuedProperty(String name, String value) {
        valuedProperties.add(new ValuedPropertyIO(name, value));
    }

    public void withValuedProperties(List<ValuedPropertyIO> valuedProperties) {
        this.valuedProperties.addAll(valuedProperties);
    }

    public void updateValuedProperty(String name, String value) {
        valuedProperties = valuedProperties.stream()
                .map(valuedProperty -> valuedProperty.getName().equals(name) ? new ValuedPropertyIO(name, value) : valuedProperty)
                .collect(Collectors.toList());
    }

    public void withIterableProperty(IterableValuedPropertyIO iterableProperty) {
        iterableProperties.add(iterableProperty);
    }

    public void withIterableProperties(List<IterableValuedPropertyIO> iterableProperties) {
        this.iterableProperties.addAll(iterableProperties);
    }

    private void withPropertiesVersionId(long propertiesVersionId) {
        this.propertiesVersionId = propertiesVersionId;
    }

    public void fromModuleBuider(ModuleBuilder moduleBuilder) {
        name = moduleBuilder.getName();
        version = moduleBuilder.getVersion();
        versionType = moduleBuilder.getVersionType();
        propertiesVersionId = 0L; // Est-ce utile ?
    }

    static List<DeployedModuleIO> buildInputs(List<DeployedModuleBuilder> deployedModuleBuilders) {
        return deployedModuleBuilders
                .stream()
                .map(DeployedModuleBuilder::buildInput)
                .collect(Collectors.toList());
    }

    private DeployedModuleIO buildInput() {
        return build(null);
    }

    static List<DeployedModuleIO> buildOutputs(List<DeployedModuleBuilder> deployedModuleBuilders) {
        return deployedModuleBuilders
                .stream()
                .map(DeployedModuleBuilder::buildOutput)
                .collect(Collectors.toList());
    }

    private DeployedModuleIO buildOutput() {
        if (StringUtils.isEmpty(modulePath)) {
            modulePath = "#";
        }
        String propertiesPath = buildPropertiesPath();
        return build(propertiesPath);
    }

    private DeployedModuleIO build(String propertiesPath) {
        return new DeployedModuleIO(
                id,
                propertiesVersionId,
                name,
                version,
                VersionType.toIsWorkingCopy(versionType),
                modulePath,
                propertiesPath,
                InstanceBuilder.build(instanceBuilders));
    }

    public PropertiesIO buildProperties() {
        return buildProperties(propertiesVersionId);
    }

    public PropertiesIO buildProperties(Long propertiesVersionId) {
        return new PropertiesIO(propertiesVersionId, new HashSet<>(valuedProperties), new HashSet<>(iterableProperties));
    }

    public String buildPropertiesPath() {
        return modulePath + "#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    static void setIds(List<DeployedModuleBuilder> deployedModuleBuilders) {
        Long maxId = deployedModuleBuilders.stream()
                .map(DeployedModuleBuilder::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L);

        for (DeployedModuleBuilder deployedModuleBuilder : deployedModuleBuilders) {
            if (deployedModuleBuilder.getId() == null || deployedModuleBuilder.getId() < 1) {
                deployedModuleBuilder.setId(++maxId);
            }
        }
    }

    void incrementPropertiesVersionId() {
        propertiesVersionId++;
    }

    public static void initPropertiesVersionIdTo(long propertiesVersionId, List<DeployedModuleBuilder> deployedModuleBuilders) {
        deployedModuleBuilders.forEach(deployedModuleBuilder -> deployedModuleBuilder.withPropertiesVersionId(propertiesVersionId));
    }

    public static void clearInstancesAndProperties(List<DeployedModuleBuilder> deployedModuleBuilders) {
        deployedModuleBuilders.forEach(DeployedModuleBuilder::clearInstancesAndProperties);
    }

    private void clearInstancesAndProperties() {
        instanceBuilders = new ArrayList<>();
        valuedProperties = new ArrayList<>();
    }

    public boolean equalsByKey(DeployedModuleBuilder deployedModuleBuilder) {
        return name.equals(deployedModuleBuilder.name) &&
                version.equals(deployedModuleBuilder.version) &&
                versionType.equals(deployedModuleBuilder.versionType) &&
                modulePath.equals(deployedModuleBuilder.modulePath);
    }

    public Optional<ModuleBuilder> findMatchingModuleBuilder(ModuleHistory moduleHistory) {
        return moduleHistory.getModuleBuilders().stream()
                .filter(moduleBuilder -> name.equals(moduleBuilder.getName()) &&
                        version.equals(moduleBuilder.getVersion()) &&
                        versionType.equals(moduleBuilder.getVersionType()))
                .findFirst();
    }
}
