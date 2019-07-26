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
package org.hesperides.core.domain.platforms.queries.views;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@AllArgsConstructor
public class PlatformView {

    String id;
    String platformName;
    String applicationName;
    String version;
    boolean isProductionPlatform;
    List<DeployedModuleView> deployedModules;
    Long versionId;
    List<ValuedPropertyView> globalProperties;
    Boolean hasPasswords;

    public PlatformView(String id, String platformName, String applicationName, String version, boolean isProductionPlatform, List<DeployedModuleView> deployedModules, Long versionId, List<ValuedPropertyView> globalProperties) {
        this.id = id;
        this.platformName = platformName;
        this.applicationName = applicationName;
        this.version = version;
        this.isProductionPlatform = isProductionPlatform;
        this.deployedModules = deployedModules;
        this.versionId = versionId;
        this.globalProperties = globalProperties;
        this.hasPasswords = null;
    }

    static List<PlatformView> setPlatformsWithPasswordIndicator(List<PlatformView> platforms, Set<Platform.Key> platformsWithPassword) {
        return platforms.stream()
                .map(platform -> platform.withPasswordIndicator(platformsWithPassword.contains(platform.getPlatformKey())))
                .collect(Collectors.toList());
    }

    public Stream<DeployedModuleView> getActiveDeployedModules() {
        return Optional.ofNullable(deployedModules)
                .orElse(Collections.emptyList())
                .stream().filter(deployedModule -> deployedModule.getId() != null && deployedModule.getId() > 0);
    }

    public Optional<DeployedModuleView> getDeployedModule(String modulePath, Module.Key moduleKey) {
        return getActiveDeployedModules()
                .filter(deployedModule -> deployedModule.getModulePath().equalsIgnoreCase(modulePath)
                        && deployedModule.getModuleKey().equals(moduleKey))
                .findFirst();
    }

    public Platform.Key getPlatformKey() {
        return new Platform.Key(applicationName, platformName);
    }

    public PlatformView withPasswordIndicator(boolean hasPasswords) {
        return new PlatformView(
                id,
                platformName,
                applicationName,
                version,
                isProductionPlatform,
                deployedModules,
                versionId,
                globalProperties,
                hasPasswords
        );
    }
}

