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
package org.hesperides.core.application.files;

import org.hesperides.core.domain.files.InstanceFileView;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FileUseCases {

    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public FileUseCases(PlatformQueries platformQueries, ModuleQueries moduleQueries) {
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
    }

    public List<InstanceFileView> getInstanceFiles(
            String applicationName,
            String platformName,
            String path,
            String moduleName,
            String moduleVersion,
            String instanceName,
            boolean isWorkingCopy,
            boolean simulate) {

        List<InstanceFileView> instanceFiles = new ArrayList<>();

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }

        //TODO Vérifier l'instance ?

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).get();
        Optional<DeployedModuleView> optionalDeployedModule = platform.getDeployedModules()
                .stream()
                .filter(deployedModule -> deployedModule.getModuleKey().equals(moduleKey))
                .findFirst();

        if (optionalDeployedModule.isPresent()) {
            moduleQueries
                    .getOptionalModule(moduleKey)
                    .get()
                    .getTemplates()
                    .forEach(template ->
                            instanceFiles.add(new InstanceFileView(platformKey, path, moduleKey, instanceName, template, simulate))
                    );
        }

        return instanceFiles;
    }
}
