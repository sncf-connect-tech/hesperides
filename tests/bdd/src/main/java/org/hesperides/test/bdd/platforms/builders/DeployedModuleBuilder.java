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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DeployedModuleBuilder implements Serializable {

    @Getter
    @Setter
    private Long id;
    private Long propertiesVersionId;
    private String name;
    private String version;
    private String versionType;
    @Getter
    private String modulePath;
    private List<ValuedPropertyIO> valuedProperties;
    private List<IterableValuedPropertyIO> iterableValuedProperties;
    private List<InstanceBuilder> instanceBuilders;

    public DeployedModuleBuilder() {
        reset();
    }

    public DeployedModuleBuilder reset() {
        id = null;
        propertiesVersionId = 0L;
        modulePath = "#ABC#DEF";
        valuedProperties = new ArrayList<>();
        iterableValuedProperties = new ArrayList<>();
        instanceBuilders = new ArrayList<>();
        return this;
    }

    public void withModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public void withInstanceBuilder(InstanceBuilder instanceBuilder) {
        instanceBuilders.add(instanceBuilder);
    }

    public void withValuedProperty(String name, String value) {
        valuedProperties.add(new ValuedPropertyIO(name, value));
    }

    public void withPropertiesVersionId(long propertiesVersionId) {
        this.propertiesVersionId = propertiesVersionId;
    }

    public void fromModuleBuider(ModuleBuilder moduleBuilder) {
        name = moduleBuilder.getName();
        version = moduleBuilder.getVersion();
        versionType = moduleBuilder.getVersionType();
        propertiesVersionId = 0L;
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
        return new PropertiesIO(propertiesVersionId, new HashSet<>(valuedProperties), new HashSet<>(iterableValuedProperties));
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

    public void incrementPropertiesVersionId() {
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
}
