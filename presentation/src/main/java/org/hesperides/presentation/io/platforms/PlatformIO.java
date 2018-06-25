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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@AllArgsConstructor
public class PlatformIO {

    @NotNull
    @NotEmpty
    @SerializedName("platform_name")
    String platformName;

    @NotNull
    @NotEmpty
    @SerializedName("application_name")
    String applicationName;

    @NotNull
    @NotEmpty
    @SerializedName("application_version")
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

    public Platform toDomainInstance() {
        return new Platform(
                new Platform.Key(applicationName, platformName, version),
                productionPlatform,
                versionId,
                DeployedModuleIO.toDomainInstances(deployedModules)
        );
    }

}
