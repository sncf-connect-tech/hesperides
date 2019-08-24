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
import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PlatformBuilder implements Serializable {

    @Getter
    private String platformName;
    @Getter
    private String applicationName;
    private String version;
    private Boolean isProductionPlatform;
    @Getter
    private List<DeployedModuleBuilder> deployedModuleBuilders;
    @Getter
    private Long versionId;
    private Long globalPropertiesVersionId;
    private List<ValuedPropertyIO> globalProperties;

    public PlatformBuilder() {
        reset();
    }

    public PlatformBuilder reset() {
        platformName = "test-platform";
        applicationName = "test-application";
        version = "1.0";
        isProductionPlatform = false;
        deployedModuleBuilders = new ArrayList<>();
        versionId = 0L;
        globalPropertiesVersionId = 0L;
        globalProperties = new ArrayList<>();
        return this;
    }

    public void withPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public void withVersion(String version) {
        this.version = version;
    }

    public void withDeployedModuleBuilder(DeployedModuleBuilder deployedModuleBuilder) {
        deployedModuleBuilders.add(SerializationUtils.clone(deployedModuleBuilder));
    }

    public void withIsProductionPlatform(Boolean isProductionPlatform) {
        this.isProductionPlatform = isProductionPlatform;
    }

    public void withGlobalProperty(String name, String value) {
        globalProperties.add(new ValuedPropertyIO(name, value));
    }

    public void withGlobalProperties(List<ValuedPropertyIO> globalProperties) {
        this.globalProperties.addAll(globalProperties);
    }

    public PlatformBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public void withGlobalPropertyVersionId(long globalPropertiesVersionId) {
        this.globalPropertiesVersionId = globalPropertiesVersionId;
    }

    public PlatformIO buildInput() {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders), versionId);
    }

    public PlatformIO buildInput(Long platformVersionId) {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders), platformVersionId);
    }

    public PlatformIO buildOutput() {
        if (isProductionPlatform == null) {
            isProductionPlatform = false;
        }
        return build(DeployedModuleBuilder.buildOutputs(deployedModuleBuilders), versionId);
    }

    private PlatformIO build(List<DeployedModuleIO> deployedModules, Long platformVersionId) {
        // On ne se préoccupe pas du flag hasPasswords dans le builder,
        // cela simplifie la glue. Le cas est géré par l'étape "the platform
        // has the password flag and the flag is set to (true|false)".
        return new PlatformIO(
                platformName,
                applicationName,
                version,
                isProductionPlatform,
                deployedModules,
                platformVersionId,
                null);
    }

    public PropertiesIO buildProperties() {
        return new PropertiesIO(globalPropertiesVersionId, new HashSet<>(globalProperties), Collections.emptySet());
    }

    public void setDeployedModuleIds() {
        DeployedModuleBuilder.setIds(deployedModuleBuilders);
    }

    public void incrementVersionId() {
        versionId++;
    }

    public void incrementGlobalPropertiesVersionId() {
        globalPropertiesVersionId++;
    }

    public void clearGlobalProperties() {
        globalProperties = new ArrayList<>();
    }

    public void clearDeployedModuleBuilders() {
        deployedModuleBuilders = new ArrayList<>();
    }

    public boolean equalsByKey(PlatformBuilder platformBuilder) {
        return applicationName.equals(platformBuilder.getApplicationName())
                && platformName.equals(platformBuilder.getPlatformName());
    }

    public InstancesModelOutput buildInstanceModel() {
        return InstanceBuilder.buildInstanceModel(deployedModuleBuilders, globalProperties);
    }

    public void updateDeployedModuleBuilder(DeployedModuleBuilder deployedModuleBuilder) {
        deployedModuleBuilder.incrementPropertiesVersionId();
        DeployedModuleBuilder updatedDeployedModuleBuilder = SerializationUtils.clone(deployedModuleBuilder);
        deployedModuleBuilders = deployedModuleBuilders.stream()
                .map(existingDeployedModuleBuilder -> existingDeployedModuleBuilder.equalsByKey(updatedDeployedModuleBuilder)
                        ? updatedDeployedModuleBuilder : existingDeployedModuleBuilder)
                .collect(Collectors.toList());
    }

    public Map<String, Set<GlobalPropertyUsageOutput>> buildGlobalPropertiesUsage(ModuleHistory moduleHistory) {
        Map<String, Set<GlobalPropertyUsageOutput>> result = new HashMap<>();
        globalProperties.forEach(globalProperty -> {
            Set<GlobalPropertyUsageOutput> globalPropertyUsage = new HashSet<>();
            deployedModuleBuilders.forEach(deployedModuleBuilder -> {

                boolean isFoundInDeployedModulePropertyValues = deployedModuleBuilder.getValuedProperties()
                        .stream()
                        .map(ValuedPropertyIO::getValue)
                        .map(PropertyBuilder::extractProperties)
                        .flatMap(List::stream)
                        .anyMatch(property -> property.equals(globalProperty.getName()));

                boolean isFoundInModulePropertyModel = deployedModuleBuilder.findMatchingModuleBuilder(moduleHistory)
                        .map(ModuleBuilder::buildPropertiesModel)
                        .map(ModelOutput::getProperties)
                        .orElseGet(Collections::emptySet)
                        .stream()
                        .map(PropertyOutput::getName)
                        .anyMatch(property -> property.equals(globalProperty.getName()));

                // Dans les propriétés d'instances ?
                if (isFoundInDeployedModulePropertyValues || isFoundInModulePropertyModel) {
                    globalPropertyUsage.add(new GlobalPropertyUsageOutput(isFoundInModulePropertyModel, deployedModuleBuilder.buildPropertiesPath()));
                }
            });
            result.put(globalProperty.getName(), globalPropertyUsage);
        });
        return result;
    }
}
