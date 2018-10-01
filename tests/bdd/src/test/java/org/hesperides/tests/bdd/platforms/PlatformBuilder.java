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
package org.hesperides.tests.bdd.platforms;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class PlatformBuilder {

    private String platformName;
    private String applicationName;
    private String version;
    private boolean isProductionPlatform;
    private List<DeployedModuleIO> deployedModuleInputs;
    private List<DeployedModuleIO> deployedModuleOutputs;
    private long versionId;

    public PlatformBuilder() {
        reset();
    }

    public PlatformBuilder reset() {
        platformName = "test-platform";
        applicationName = "test-application";
        version = "1.0";
        isProductionPlatform = false;
        deployedModuleInputs = new ArrayList<>();
        deployedModuleOutputs = new ArrayList<>();
        versionId = 1;
        return this;
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

    public PlatformBuilder withProductionPlatform(boolean productionPlatform) {
        isProductionPlatform = productionPlatform;
        return this;
    }

    public PlatformBuilder withModule(ModuleIO module, String propertiesPath) {
        deployedModuleInputs.add(new DeployedModuleIO(0L, module.getName(), module.getVersion(), module.isWorkingCopy(), "GROUP", propertiesPath, null));
        deployedModuleOutputs.add(new DeployedModuleIO(1L, module.getName(), module.getVersion(), module.isWorkingCopy(), "GROUP", propertiesPath, Collections.emptyList()));
        return this;
    }

    public PlatformBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public PlatformIO buildInput() {
        return new PlatformIO(platformName, applicationName, version, isProductionPlatform, deployedModuleInputs, versionId);
    }

    public PlatformIO buildOutput() {
        return buildOutput(false);
    }

    public PlatformIO buildOutput(boolean hidePlatform) {
        List<DeployedModuleIO> modules = hidePlatform ? Collections.emptyList() : deployedModuleOutputs;
        return new PlatformIO(platformName, applicationName, version, isProductionPlatform, modules, versionId);
    }

    public ApplicationOutput buildApplicationOutput(boolean hidePlatform) {
        return new ApplicationOutput(applicationName, Arrays.asList(buildOutput(hidePlatform)));
    }
}
