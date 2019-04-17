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
package org.hesperides.core.presentation.io.platforms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Value
@AllArgsConstructor
public class PlatformIO {

    @OnlyPrintableCharacters(subject = "platform_name")
    @SerializedName("platform_name")
    @JsonProperty("platform_name")
    String platformName;
    @OnlyPrintableCharacters(subject = "application_name")
    @SerializedName("application_name")
    @JsonProperty("application_name")
    String applicationName;
    @OnlyPrintableCharacters(subject = "application_version")
    @SerializedName("application_version")
    @JsonProperty("application_version")
    String version;
    @NotNull
    @SerializedName("production")
    @JsonProperty("production")
    Boolean isProductionPlatform;
    @SerializedName("modules")
    @JsonProperty("modules")
    List<DeployedModuleIO> deployedModules;
    @NotNull
    @SerializedName("version_id")
    @JsonProperty("version_id")
    Long versionId;

    public PlatformIO(PlatformView platformView) {
        this(platformView, false);
    }

    public PlatformIO(PlatformView platformView, boolean hidePlatformsModules) {
        platformName = platformView.getPlatformName();
        applicationName = platformView.getApplicationName();
        version = platformView.getVersion();
        isProductionPlatform = platformView.isProductionPlatform();
        deployedModules = hidePlatformsModules ? Collections.emptyList() : DeployedModuleIO.fromActiveDeployedModuleViews(platformView.getActiveDeployedModules());
        versionId = platformView.getVersionId();
    }

    public Platform toDomainInstance() {
        return new Platform(
                new Platform.Key(applicationName, platformName),
                version,
                isProductionPlatform != null ? isProductionPlatform : false,
                versionId,
                DeployedModuleIO.toDomainInstances(deployedModules),
                Collections.emptyList()
        );
    }

    public static List<PlatformIO> fromPlatformViews(List<PlatformView> platformViews, boolean hidePlatformsModules) {
        return Optional.ofNullable(platformViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(platformView -> new PlatformIO(platformView, hidePlatformsModules))
                .collect(toList());
    }
}
