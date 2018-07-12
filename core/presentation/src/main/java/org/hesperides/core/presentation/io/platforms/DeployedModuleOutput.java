package org.hesperides.core.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class DeployedModuleOutput {

    Long id;
    String name;
    String version;
    @SerializedName("working_copy")
    boolean isWorkingCopy;
    @SerializedName("properties_path")
    String propertiesPath;
    String path;
    List<InstanceIO> instances;

    public DeployedModuleOutput(DeployedModuleView deployedModuleView) {
        this.id = deployedModuleView.getId();
        this.name = deployedModuleView.getName();
        this.version = deployedModuleView.getVersion();
        this.isWorkingCopy = deployedModuleView.isWorkingCopy();
        this.propertiesPath = deployedModuleView.getPropertiesPath();
        this.path = deployedModuleView.getPath();
        this.instances = InstanceIO.fromInstanceViews(deployedModuleView.getInstances());
    }

    public static List<DeployedModuleOutput> fromDeployedModuleViews(List<DeployedModuleView> deployedModuleViews) {
        return Optional.ofNullable(deployedModuleViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(DeployedModuleOutput::new)
                .collect(Collectors.toList());
    }
}
