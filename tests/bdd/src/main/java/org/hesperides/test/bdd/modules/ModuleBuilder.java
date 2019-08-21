/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
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
package org.hesperides.test.bdd.modules;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateContainerBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ModuleBuilder extends TemplateContainerBuilder {

    private List<TechnoBuilder> technoBuilders;
    private Long versionId;

    public ModuleBuilder() {
        reset();
    }

    public void reset() {
        reset("test-module");
        technoBuilders = new ArrayList<>();
        versionId = 0L;
    }

    public ModuleIO build() {
        List<TechnoIO> technos = technoBuilders.stream().map(TechnoBuilder::build).collect(Collectors.toList());
        return new ModuleIO(name, version, VersionType.toIsWorkingCopy(versionType), technos, versionId);
    }

    public String buildNamespace() {
        return "modules#" + name + "#" + version + "#" + StringUtils.upperCase(versionType);
    }

    public void withVersionId(long versionId) {
        this.versionId = versionId;
    }

    public void incrementVersionId() {
        versionId++;
    }

    public void withTechnoBuilder(TechnoBuilder technoBuilder) {
        technoBuilders.add(SerializationUtils.clone(technoBuilder));
    }

    @Override
    public ModelOutput buildPropertiesModel() {
        List<ModelOutput> technosModel = technoBuilders.stream().map(TemplateContainerBuilder::buildPropertiesModel).collect(Collectors.toList());
        Set<PropertyOutput> technosSimpleProperties = technosModel.stream().map(ModelOutput::getProperties).flatMap(Set::stream).collect(Collectors.toSet());
        Set<PropertyOutput> technosIterableProperties = technosModel.stream().map(ModelOutput::getIterableProperties).flatMap(Set::stream).collect(Collectors.toSet());

        Set<PropertyOutput> moduleSimpleProperties = super.buildPropertiesModel().getProperties();
        moduleSimpleProperties.addAll(technosSimpleProperties);
        Set<PropertyOutput> moduleIterableProperties = super.buildPropertiesModel().getIterableProperties();
        moduleIterableProperties.addAll(technosIterableProperties);

        return new ModelOutput(moduleSimpleProperties, moduleIterableProperties);
    }

    public ModuleKeyOutput buildModuleKeyOutput() {
        return new ModuleKeyOutput(name, version, VersionType.toIsWorkingCopy(versionType));
    }
}
