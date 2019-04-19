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
package org.hesperides.test.bdd.platforms;

import lombok.Value;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.*;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class PlatformBuilder {

    private String platformName;
    private String applicationName;
    private String version;
    private Boolean isProductionPlatform;
    private List<DeployedModuleIO> deployedModules;
    private long versionId;

    private List<Property> properties;
    private List<IterableValuedPropertyIO> iterableProperties;
    private Set<String> instanceProperties;
    private List<InstanceIO> instances;
    private List<ValuedPropertyIO> instancePropertyValues;

    public PlatformBuilder() {
        reset();
    }

    public PlatformBuilder reset() {
        platformName = "test-platform";
        applicationName = "test-application";
        version = "1.0";
        isProductionPlatform = false;
        deployedModules = new ArrayList<>();
        versionId = 1;
        properties = new ArrayList<>();
        iterableProperties = new ArrayList<>();
        instanceProperties = new HashSet<>();
        instances = new ArrayList<>();
        instancePropertyValues = new ArrayList<>();
        return this;
    }

    public String getPlatformName() {
        return platformName;
    }

    public PlatformBuilder withPlatformName(String platformName) {
        this.platformName = platformName;
        return this;
    }

    public PlatformBuilder withApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public PlatformBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public PlatformBuilder withIsProductionPlatform(Boolean isProductionPlatform) {
        this.isProductionPlatform = isProductionPlatform;
        return this;
    }

    public void withInstance(String name, List<ValuedPropertyIO> properties) {
        instances.add(new InstanceIO(name, new HashSet<>(properties)));
    }

    public void withInstance(String name) {
        withInstance(name, getInstancePropertyValues());
    }

    public List<DeployedModuleIO> getDeployedModules() {
        return deployedModules;
    }

    public void incrementDeployedModuleIds() {
        deployedModules = deployedModules.stream()
                .map(dm -> new DeployedModuleIO(
                        dm.getId() + 1,
                        dm.getName(),
                        dm.getVersion(),
                        dm.getIsWorkingCopy(),
                        dm.getModulePath(),
                        dm.getPropertiesPath(),
                        dm.getInstances()
                ))
                .collect(Collectors.toList());
    }

    public void setDeployedModulesVersion(String version) {
        deployedModules = deployedModules.stream()
                .map(dm -> new DeployedModuleIO(
                        dm.getId(),
                        dm.getName(),
                        version,
                        dm.getIsWorkingCopy(),
                        dm.getModulePath(),
                        dm.getPropertiesPath(),
                        dm.getInstances()
                ))
                .collect(Collectors.toList());
    }

    public PlatformBuilder withModule(ModuleIO module, String propertiesPath, String logicalGroup) {
        String modulePath = "#GROUP";
        if ("".equals(logicalGroup) || "#".equals(logicalGroup)) {
            modulePath = logicalGroup;
        } else if (logicalGroup != null) {
            modulePath = "#" + logicalGroup;
        }
        deployedModules.add(new DeployedModuleIO(0L, module.getName(), module.getVersion(), module.getIsWorkingCopy(), modulePath, propertiesPath, instances));
        return this;
    }

    public PlatformBuilder withNoModule() {
        deployedModules = new ArrayList<>();
        return this;
    }

    public PlatformBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public PlatformIO buildInput() {
        return new PlatformIO(platformName, applicationName, version, isProductionPlatform, deployedModules, versionId);
    }

    public PlatformIO buildOutput() {
        return buildOutput(true);
    }

    public PlatformIO buildOutputWithoutModules() {
        return buildOutput(false);
    }

    private PlatformIO buildOutput(boolean includeModules) {
        List<DeployedModuleIO> modules;
        if (!includeModules) {
            modules = Collections.emptyList();
        } else {
            AtomicLong moduleId = new AtomicLong();
            modules = deployedModules.stream().map(module ->
                    new DeployedModuleIO(moduleId.incrementAndGet(), module.getName(), module.getVersion(), module.getIsWorkingCopy(), module.getModulePath(), module.getPropertiesPath(), module.getInstances())
            ).collect(Collectors.toList());
        }
        return new PlatformIO(platformName, applicationName, version, isProductionPlatform, modules, versionId);
    }

    public ApplicationOutput buildApplicationOutput(boolean hidePlatform) {
        PlatformIO platform = hidePlatform ? buildOutputWithoutModules() : buildOutput();
        return new ApplicationOutput(applicationName, Arrays.asList(platform));
    }

    public void withGlobalProperty(String name, String value, ModelBuilder modelBuilder) {
        boolean isUsed = modelBuilder.containsProperty(name);
        properties.add(new Property(name, value, true, isUsed, false));
    }

    public void withGlobalProperty(String name, String value, boolean isUsed, boolean isRemovedFromTemplate) {
        properties.add(new Property(name, value, true, isUsed, isRemovedFromTemplate));
    }

    public void withProperty(String name, String value) {
        properties.add(new Property(name, value, false, false, false));
    }

    public void setProperty(String name, String value) {
        properties = properties.stream()
                .map(p -> {
                    if (p.getName().equals(name)) {
                        p = p.copyWithValue(value);
                    }
                    return p;
                })
                .collect(Collectors.toList());
    }

    public void withInstanceProperty(String propertyName, String... instancePropertyNames) {
        StringBuilder propertyValue = new StringBuilder();
        Arrays.stream(instancePropertyNames).forEach(instancePropertyName -> {
            instanceProperties.add(instancePropertyName);
            propertyValue.append("{{ " + instancePropertyName + " }}");
        });
        withProperty(propertyName, propertyValue.toString());
    }

    public void incrementVersionId() {
        versionId++;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public PropertiesIO getPropertiesIO(boolean isGlobal) {
        return new PropertiesIO(
                new HashSet<>(getValuedProperties(isGlobal)),
                new HashSet<>(iterableProperties));
    }

    public List<ValuedPropertyIO> getValuedProperties(boolean isGlobal) {
        return properties
                .stream()
                .filter(property -> property.isGlobal() == isGlobal)
                .map(property -> new ValuedPropertyIO(property.name, property.value))
                .collect(Collectors.toList());
    }

    public List<ValuedPropertyIO> getModuleAndGlobalProperties() {
        return properties
                .stream()
                .map(property -> new ValuedPropertyIO(property.name, property.value))
                .collect(Collectors.toList());
    }

    public List<ValuedPropertyIO> getAllGlobalProperties() {
        return getValuedProperties(true);
    }

    public void withIterableProperties(List<IterableValuedPropertyIO> iterableProperties) {
        this.iterableProperties.addAll(iterableProperties);
    }

    public void withInstancePropertyValue(String name, String value) {
        instancePropertyValues.add(new ValuedPropertyIO(name, value));
    }

    public InstancesModelOutput buildInstancesModel() {
        return new InstancesModelOutput(
                instanceProperties.stream()
                        .map(propertyName -> new InstancesModelOutput.InstancePropertyOutput(
                                propertyName, "", false, null, null, false))
                        .collect(Collectors.toSet()));
    }

    public List<ValuedPropertyIO> getInstancePropertyValues() {
        return instancePropertyValues;
    }

    public Boolean getIsProductionPlatform() {
        return isProductionPlatform;
    }

    @Value
    public static class Property {
        String name;
        String value;
        boolean isGlobal;
        boolean isUsed;
        boolean isRemovedFromTemplate;

        Property copyWithValue(String newValue) {
            return new Property(
                    name,
                    newValue,
                    isGlobal,
                    isUsed,
                    isRemovedFromTemplate
            );
        }
    }
}
