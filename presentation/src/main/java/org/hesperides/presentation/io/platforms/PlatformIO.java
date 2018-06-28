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
package org.hesperides.presentation.io.platforms;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;

import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hesperides.presentation.io.OnlyPrintableCharacters;

@Value
@AllArgsConstructor
public class PlatformIO {

    @OnlyPrintableCharacters(subject = "platform_name")
    @SerializedName("platform_name")
    String platformName;

    @OnlyPrintableCharacters(subject = "application_name")
    @SerializedName("application_name")
    String applicationName;

    @OnlyPrintableCharacters(subject = "version")
    String version;

    @SerializedName("production")
    boolean productionPlatform;

    List<DeployedModuleIO> deployedModules;

    @SerializedName("version_id")
    Long versionId;

    public PlatformIO(PlatformView platformView) {
        this.platformName = platformView.getPlatformName();
        this.applicationName = platformView.getApplicationName();
        this.version = platformView.getVersion();
        this.productionPlatform = platformView.isProductionPlatform();
        this.deployedModules = DeployedModuleIO.fromDeployedModuleViews(platformView.getDeployedModules());
        this.versionId = platformView.getVersionId();
    }

    public static List<PlatformIO> fromPlatformViews(List<PlatformView> platformViews) {
        return Optional.ofNullable(platformViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(PlatformIO::new)
                .collect(Collectors.toList());
    }

    public Platform toDomainInstance() {
        return new Platform(
                new Platform.Key(applicationName, platformName),
                version,
                productionPlatform,
                versionId,
                DeployedModuleIO.toDomainInstances(deployedModules)
        );
    }

}
