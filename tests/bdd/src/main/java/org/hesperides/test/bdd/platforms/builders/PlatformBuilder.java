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
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
    @Getter
    private Long globalPropertiesVersionId;
    @Getter
    @Setter
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

    public void withApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public PlatformBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public void withGlobalPropertyVersionId(long globalPropertiesVersionId) {
        this.globalPropertiesVersionId = globalPropertiesVersionId;
    }

    public PlatformIO buildInput() {
        return buildInputWithPlatformVersionId(versionId);
    }

    public PlatformIO buildInputWithPlatformVersionId(Long platformVersionId) {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders), platformVersionId, platformName);
    }

    public PlatformIO buildInputWithPlatformName(String platformName) {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders), versionId, platformName);
    }

    public PlatformIO buildOutput() {
        return buildOutput(false);
    }

    public PlatformIO buildOutput(boolean withoutModules) {
        if (isProductionPlatform == null) {
            isProductionPlatform = false;
        }
        return build(DeployedModuleBuilder.buildOutputs(withoutModules ? Collections.emptyList() : deployedModuleBuilders), versionId, platformName);
    }

    private PlatformIO build(List<DeployedModuleIO> deployedModules, Long platformVersionId, String platformName) {
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
        return buildProperties(globalPropertiesVersionId);
    }

    public PropertiesIO buildProperties(Long globalPropertiesVersionId) {
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
        globalProperties.stream().map(ValuedPropertyIO::getName).forEach(globalPropertyName -> {

            Set<GlobalPropertyUsageOutput> globalPropertyUsage = new HashSet<>();
            deployedModuleBuilders.forEach(deployedModuleBuilder -> {

                boolean isFoundInDeployedModulePropertyValues = deployedModuleBuilder.getValuedProperties()
                        .stream()
                        .anyMatch(valuedProperty -> globalPropertyIsFoundInValues(globalPropertyName, valuedProperty));

                if (!isFoundInDeployedModulePropertyValues) {
                    // Si on ne la trouve pas dans les propriétés simples,
                    // on la recherche dans les propriétés itérables
                    isFoundInDeployedModulePropertyValues = deployedModuleBuilder.getIterableProperties()
                            .stream()
                            .anyMatch(valuedProperty -> globalPropertyIsFoundInValues(globalPropertyName, valuedProperty));
                }

                Optional<ModelOutput> modelOutput = deployedModuleBuilder.findMatchingModuleBuilder(moduleHistory).map(ModuleBuilder::buildPropertiesModel);
                Set<PropertyOutput> simpleProperties = modelOutput.map(ModelOutput::getProperties).orElseGet(Collections::emptySet);
                boolean isFoundInModulePropertyModel = globalPropertyIsFoundInProperties(globalPropertyName, simpleProperties);

                if (!isFoundInModulePropertyModel) {
                    // Même chose
                    Set<PropertyOutput> iterableProperties = modelOutput.map(ModelOutput::getIterableProperties).orElseGet(Collections::emptySet);
                    isFoundInModulePropertyModel = globalPropertyIsFoundInProperties(globalPropertyName, iterableProperties);
                }

                if (isFoundInDeployedModulePropertyValues || isFoundInModulePropertyModel) {
                    globalPropertyUsage.add(new GlobalPropertyUsageOutput(isFoundInModulePropertyModel, deployedModuleBuilder.buildPropertiesPath()));
                }
            });
            result.put(globalPropertyName, globalPropertyUsage);
        });
        return result;
    }

    private static boolean globalPropertyIsFoundInValues(String globalPropertyName, AbstractValuedPropertyIO abstractValuedProperty) {
        boolean found = false;
        if (abstractValuedProperty instanceof ValuedPropertyIO) {

            ValuedPropertyIO valuedProperty = (ValuedPropertyIO) abstractValuedProperty;
            found = PropertyBuilder.extractProperties(valuedProperty.getValue())
                    .stream()
                    .anyMatch(extractedPropertyName -> extractedPropertyName.equals(globalPropertyName));

        } else if (abstractValuedProperty instanceof IterableValuedPropertyIO) {

            IterableValuedPropertyIO iterableValuedProperty = (IterableValuedPropertyIO) abstractValuedProperty;
            found = iterableValuedProperty.getIterablePropertyItems()
                    .stream()
                    .map(IterablePropertyItemIO::getAbstractValuedProperties)
                    .flatMap(List::stream)
                    .anyMatch(itemProperty -> globalPropertyIsFoundInValues(globalPropertyName, itemProperty));
        }
        return found;
    }

    private static boolean globalPropertyIsFoundInProperties(String globalPropertyName, Set<PropertyOutput> properties) {
        return properties.stream().anyMatch(property -> globalPropertyIsFoundInProperty(globalPropertyName, property));
    }

    private static boolean globalPropertyIsFoundInProperty(String globalPropertyName, PropertyOutput property) {
        boolean found;
        if (CollectionUtils.isEmpty(property.getProperties())) {
            found = property.getName().equals(globalPropertyName);
        } else {
            found = property.getProperties()
                    .stream()
                    .anyMatch(propertyOutput -> globalPropertyIsFoundInProperty(globalPropertyName, propertyOutput));
        }
        return found;
    }

    public List<ValuedPropertyIO> getAllModuleProperties() {
        return deployedModuleBuilders.stream()
                .map(DeployedModuleBuilder::getValuedProperties)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<ValuedPropertyIO> getAllInstanceProperties() {
        return deployedModuleBuilders.stream()
                .map(DeployedModuleBuilder::getInstanceBuilders)
                .flatMap(List::stream)
                .map(InstanceBuilder::getValuedProperties)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Optional<DeployedModuleBuilder> findMatchingDeployedModuleBuilder(ModuleBuilder moduleBuilder) {
        return deployedModuleBuilders.stream().filter(deployedModuleBuilder ->
                deployedModuleBuilder.matchModuleBuilder(moduleBuilder))
                .findFirst();
    }

    public Platform.Key buildPlatformKey() {
        return new Platform.Key(applicationName, platformName);
    }

    public DeployedModuleBuilder findDeployedModuleBuilderByName(String moduleName) {
        return deployedModuleBuilders.stream()
                .filter(builder -> moduleName.equals(builder.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find deployed module with name \"" + moduleName + "\""));
    }

    public DeployedModuleBuilder findDeployedModuleBuilderByVersion(String moduleVersion) {
        return deployedModuleBuilders.stream()
                .filter(deployedModuleBuilder -> moduleVersion.equals(deployedModuleBuilder.getVersion()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find deployed module with version \"" + moduleVersion + "\""));
    }
}
