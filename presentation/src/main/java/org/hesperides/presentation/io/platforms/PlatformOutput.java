package org.hesperides.presentation.io.platforms;

import static java.util.stream.Collectors.toList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;

import org.hesperides.domain.platforms.queries.views.PlatformView;

@Value
@AllArgsConstructor
public class PlatformOutput {
    @SerializedName("platform_name")
    String platformName;

    @SerializedName("application_name")
    String applicationName;

    List<DeployedModuleOutput> deployedModules;

    boolean productionPlatform;

    String version;

    Long versionId;

    public PlatformOutput(PlatformView platformView) {
        this.platformName = platformView.getPlatformName();
        this.applicationName = platformView.getApplicationName();
        this.version = platformView.getVersion();
        this.productionPlatform = platformView.isProductionPlatform();
        this.deployedModules = DeployedModuleOutput.fromViews(platformView.getDeployedModules());
        this.versionId = platformView.getVersionId();
    }

    public static List<PlatformOutput> fromViews(List<PlatformView> platformViews) {
        return Optional.ofNullable(platformViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(PlatformOutput::new)
                .collect(toList());
    }

}
