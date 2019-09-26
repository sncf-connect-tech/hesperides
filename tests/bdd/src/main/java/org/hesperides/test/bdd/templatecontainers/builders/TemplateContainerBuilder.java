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
package org.hesperides.test.bdd.templatecontainers.builders;

import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.templatecontainers.VersionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class TemplateContainerBuilder implements Serializable {

    //TODO mettre à jour les proprétés en même temps que les templates ?

    @Getter
    protected String name;
    @Getter
    protected String version;
    @Getter
    protected String versionType;
    @Getter
    private List<TemplateBuilder> templateBuilders;
    private List<PropertyBuilder> propertyBuilders;

    protected void reset(String name) {
        this.name = name;
        version = "1.0";
        versionType = VersionType.WORKINGCOPY;
        templateBuilders = new ArrayList<>();
        propertyBuilders = new ArrayList<>();
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

    protected abstract String buildNamespace();

    public ModelOutput buildPropertiesModel() {
        Set<PropertyOutput> simpleProperties = propertyBuilders.stream()
                .filter(PropertyBuilder::isSimpleProperty)
                .map(PropertyBuilder::build)
                .collect(Collectors.toSet());
        Set<PropertyOutput> iterableProperties = propertyBuilders.stream()
                .filter(PropertyBuilder::isIterableProperty)
                .map(PropertyBuilder::build)
                .collect(Collectors.toSet());
        return new ModelOutput(simpleProperties, iterableProperties);
    }

    public void addPropertyBuilder(PropertyBuilder propertyBuilder) {
        propertyBuilders.add(SerializationUtils.clone(propertyBuilder));
    }

    public void addTemplateBuilder(TemplateBuilder templateBuilder) {
        templateBuilder.incrementVersionId();
        templateBuilder.withNamespace(buildNamespace());
        TemplateBuilder templateBuilderInstance = SerializationUtils.clone(templateBuilder);
        templateBuilders.add(templateBuilderInstance);
    }

    public void removeTemplateBuilder(String templateName) {
        templateBuilders = templateBuilders.stream()
                .filter(templateBuilder -> !templateBuilder.getName().equals(templateName))
                .collect(Collectors.toList());
    }

    public void updateTemplateBuilder(TemplateBuilder templateBuilder) {
        templateBuilder.incrementVersionId();
        TemplateBuilder updatedTemplateBuilder = SerializationUtils.clone(templateBuilder);
        templateBuilders = templateBuilders.stream()
                .map(existingTemplateBuilder -> existingTemplateBuilder.getName().equals(updatedTemplateBuilder.getName())
                        ? updatedTemplateBuilder : existingTemplateBuilder)
                .collect(Collectors.toList());
    }

    public void updateTemplatesNamespace() {
        templateBuilders = templateBuilders.stream()
                .map(templateBuilder -> templateBuilder.withNamespace(buildNamespace()))
                .collect(Collectors.toList());
    }

    public TemplateBuilder getLastTemplateBuilder() {
        return templateBuilders.get(templateBuilders.size() - 1);
    }

    public boolean equalsByKey(TemplateContainerBuilder templateContainerBuilder) {
        return name.equals(templateContainerBuilder.getName()) &&
                version.equals(templateContainerBuilder.getVersion()) &&
                versionType.equals(templateContainerBuilder.getVersionType());
    }

    public boolean isWorkingCopy() {
        return VersionType.toIsWorkingCopy(versionType);
    }

    public boolean isPasswordProperty(String propertyName) {
        return propertyBuilders.stream()
                .anyMatch(propertyBuilder -> propertyBuilder.isPassword() &&
                        propertyBuilder.getName().equals(propertyName));
    }
}
