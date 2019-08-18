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

import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeployedModuleBuilder implements Serializable {

    private Long id;
    private Long propertiesVersionId;
    private String name;
    private String version;
    private String versionType;
    private String modulePath;
    private String propertiesPath;
    private List<ValuedPropertyBuilder> valuedPropertyBuilders;
    private List<InstanceBuilder> instanceBuilders;

    public DeployedModuleBuilder() {
        reset();
    }

    public DeployedModuleBuilder reset() {
        id = null; //ou 0
        propertiesVersionId = 0L; //ou null
//        name = "test-module";
//        version = "1.0";
//        versionType = VersionType.WORKINGCOPY;
        modulePath = "#ABC#DEF";
//        propertiesPath = ""; // ou null ou pas du tout
        valuedPropertyBuilders = new ArrayList<>();
        instanceBuilders = new ArrayList<>();
        return this;
    }

    public void withInstanceBuilder(InstanceBuilder instanceBuilder) {
        instanceBuilders.add(instanceBuilder);
    }

    public void fromModuleBuider(ModuleBuilder moduleBuilder) {
        name = moduleBuilder.getName();
        version = moduleBuilder.getVersion();
        versionType = moduleBuilder.getVersionType();
    }

    static List<DeployedModuleIO> buildInputs(List<DeployedModuleBuilder> deployedModuleBuilders) {
        return deployedModuleBuilders.stream().map(DeployedModuleBuilder::buildInput).collect(Collectors.toList());
    }

    private DeployedModuleIO buildInput() {
        return build(null);
    }

    static List<DeployedModuleIO> buildOutputs(List<DeployedModuleBuilder> deployedModuleBuilders) {
        return deployedModuleBuilders.stream().map(DeployedModuleBuilder::buildOutput).collect(Collectors.toList());
    }

    private DeployedModuleIO buildOutput() {
        String propertiesPath = modulePath + "#" + name + "#" + version + "#" + versionType.toUpperCase();
        return build(propertiesPath);
    }

    private DeployedModuleIO build(String propertiesPath) { //id ?
        return new DeployedModuleIO(id, propertiesVersionId, name, version, VersionType.toIsWorkingCopy(versionType), modulePath, propertiesPath, InstanceBuilder.build(instanceBuilders));
    }
}
