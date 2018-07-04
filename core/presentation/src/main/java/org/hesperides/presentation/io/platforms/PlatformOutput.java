package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.platforms.queries.views.PlatformView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Value
@AllArgsConstructor
public class PlatformOutput {

    @SerializedName("platform_name")
    String platformName;
    @SerializedName("application_name")
    String applicationName;
    @SerializedName("modules")
    List<DeployedModuleOutput> deployedModules;
    @SerializedName("production")
    boolean isProductionPlatform;
    String version;
    Long versionId;

    public PlatformOutput(PlatformView platformView) {
        this.platformName = platformView.getPlatformName();
        this.applicationName = platformView.getApplicationName();
        this.version = platformView.getVersion();
        this.isProductionPlatform = platformView.isProductionPlatform();
        this.deployedModules = DeployedModuleOutput.fromDeployedModuleViews(platformView.getDeployedModules());
        this.versionId = platformView.getVersionId();
    }

    public static List<PlatformOutput> fromPlatformViews(List<PlatformView> platformViews) {
        return Optional.ofNullable(platformViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(PlatformOutput::new)
                .collect(toList());
    }
}
