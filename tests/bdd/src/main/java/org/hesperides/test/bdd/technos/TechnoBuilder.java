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
package org.hesperides.test.bdd.technos;

import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TechnoBuilder implements Serializable {

    @Getter
    private String name;
    private String version;
    @Getter
    private String versionType;
    @Getter
    private List<TemplateBuilder> templateBuilders;
    private List<PropertyBuilder> propertyBuilders;

    public TechnoBuilder() {
        reset();
    }

    public void reset() {
        name = "test-techno";
        version = "1.0";
        versionType = VersionType.WORKINGCOPY;
        templateBuilders = new ArrayList<>();
        propertyBuilders = new ArrayList<>();
    }

    public TechnoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TechnoBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public void withVersionType(String versionType) {
        this.versionType = versionType;
    }

    public TechnoIO build() {
        return new TechnoIO(name, version, VersionType.toIsWorkingCopy(versionType));
    }

    public String buildNamespace() {
        return "packages#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

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

    public void saveTemplateBuilderInstance(TemplateBuilder templateBuilder) {
        templateBuilder.incrementVersionId();
        templateBuilder.withNamespace(buildNamespace());
        TemplateBuilder templateBuilderInstance = SerializationUtils.clone(templateBuilder);
        templateBuilders.add(templateBuilderInstance);
    }

    public void savePropertyBuilderInstance(PropertyBuilder propertyBuilder) {
        propertyBuilders.add(SerializationUtils.clone(propertyBuilder));
    }

    public void removeTemplateBuilderInstance(String templateName) {
        templateBuilders = templateBuilders.stream()
                .filter(templateBuilder -> !templateBuilder.getName().equals(templateName))
                .collect(Collectors.toList());
    }

    public void updateTemplateBuilderInstance(TemplateBuilder updatedTemplateBuilder) {
        updatedTemplateBuilder.incrementVersionId();
        TemplateBuilder updatedTemplateBuilderInstance = SerializationUtils.clone(updatedTemplateBuilder);
        templateBuilders = templateBuilders.stream()
                .map(existingTemplateBuilder -> existingTemplateBuilder.getName().equals(updatedTemplateBuilderInstance.getName())
                        ? updatedTemplateBuilderInstance : existingTemplateBuilder)
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
}
