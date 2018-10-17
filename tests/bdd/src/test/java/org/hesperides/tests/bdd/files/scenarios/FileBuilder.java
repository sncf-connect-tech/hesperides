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
package org.hesperides.tests.bdd.files.scenarios;

import lombok.Getter;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class FileBuilder {

    private List<InstanceFileOutput> instanceFiles;

    public FileBuilder() {
        reset();
    }

    public void reset() {
        instanceFiles = new ArrayList<>();
    }

    public FileBuilder withInstanceFile(InstanceFileOutput instanceFile) {
        instanceFiles.add(instanceFile);
        return this;
    }

    public List<InstanceFileOutput> buildInstanceFiles() {
        return instanceFiles;
    }

    private String applicationName;
    private String platformName;
    private String path;
    private String moduleName;
    private String moduleVersion;
    private String instanceName;
    private boolean moduleWorkingCopy;
    private Boolean simulate;

    public FileBuilder withApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public FileBuilder withPlatformName(String platformName) {
        this.platformName = platformName;
        return this;
    }

    public FileBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public FileBuilder withModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public FileBuilder withModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
        return this;
    }

    public FileBuilder withInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public FileBuilder withModuleWorkingCopy(boolean moduleWorkingCopy) {
        this.moduleWorkingCopy = moduleWorkingCopy;
        return this;
    }

    public FileBuilder withSimulate(boolean simulate) {
        this.simulate = simulate;
        return this;
    }
}
