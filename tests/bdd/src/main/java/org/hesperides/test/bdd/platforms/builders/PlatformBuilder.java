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
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private Long versionId;
    private Boolean hasPasswords;
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
        deployedModuleBuilders.add(deployedModuleBuilder);
    }

    public PlatformIO buildInput() {
        return build(DeployedModuleBuilder.buildInputs(deployedModuleBuilders));
    }

    public PlatformIO buildOutput() {
        if (isProductionPlatform == null) {
            isProductionPlatform = false;
        }
        return build(DeployedModuleBuilder.buildOutputs(deployedModuleBuilders));
    }

    private PlatformIO build(List<DeployedModuleIO> deployedModules) {
        return new PlatformIO(
                platformName,
                applicationName,
                version,
                isProductionPlatform,
                deployedModules,
                versionId,
                hasPasswords);
    }

    public void setDeployedModuleIds() {
        DeployedModuleBuilder.setIds(deployedModuleBuilders);
    }

    public void incrementVersionId() {
        versionId++;
    }

    public void withIsProductionPlatform(Boolean isProductionPlatform) {
        this.isProductionPlatform = isProductionPlatform;
    }
}
