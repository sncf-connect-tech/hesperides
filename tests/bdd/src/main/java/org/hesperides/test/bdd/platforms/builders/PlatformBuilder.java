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
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlatformBuilder implements Serializable {

    private String platformName;
    private String applicationName;
    private String version;
    private Boolean isProductionPlatform;
    private List<DeployedModuleBuilder> deployedModuleBuilders;
    private Long versionId;
    private Long globalPropertiesVersionId;
    private List<PropertyBuilder> propertyBuilders;

    public PlatformBuilder() {
        reset();
    }

    public PlatformBuilder reset() {
        platformName = "test-platform";
        applicationName = "test-application";
        version = "1.0";
        isProductionPlatform = false;
        deployedModuleBuilders = new ArrayList<>();
        versionId = 1L;
        globalPropertiesVersionId = 0L;
        propertyBuilders = new ArrayList<>();
        return this;
    }

    public void withDeployedModuleBuilder(DeployedModuleBuilder deployedModuleBuilder) {
        deployedModuleBuilders.add(deployedModuleBuilder);
    }

    public PlatformIO buildInput() {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders), null);
    }

    public PlatformIO buildOutput() {
        return build(DeployedModuleBuilder.buildOutputs(deployedModuleBuilders), null);
    }

    private PlatformIO build(List<DeployedModuleIO> deployedModules, Boolean hasPasswords) {
        return new PlatformIO(
                platformName,
                applicationName,
                version,
                isProductionPlatform,
                deployedModules,
                versionId,
                hasPasswords);
    }

    public void updateDeployedModulesId() {
        //Récupérer l'identifiant le plus élevé
        deployedModuleBuilders.forEach(deployedModuleBuilder -> {
        });
    }
}
