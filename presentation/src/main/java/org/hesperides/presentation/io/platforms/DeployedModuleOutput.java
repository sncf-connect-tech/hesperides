package org.hesperides.presentation.io.platforms;

import java.util.List;
import static java.util.stream.Collectors.toList;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;

import org.hesperides.domain.platforms.queries.views.DeployedModuleView;

@Value
@AllArgsConstructor
public class DeployedModuleOutput {

    Long id;

    String name;

    String version;

    @SerializedName("working_copy")
    boolean workingCopy;

    @SerializedName("properties_path")
    String propertiesPath;

    String path;

    List<InstanceIO> instances;

    public DeployedModuleOutput(DeployedModuleView deployedModuleView) {
        this.id = deployedModuleView.getId();
        this.name = deployedModuleView.getName();
        this.version = deployedModuleView.getVersion();
        this.workingCopy = deployedModuleView.isWorkingCopy();
        this.propertiesPath = deployedModuleView.getPropertiesPath();
        this.path = deployedModuleView.getPath();
        this.instances = InstanceIO.fromInstanceViews(deployedModuleView.getInstances());
    }

    public static List<DeployedModuleOutput> fromViews(List<DeployedModuleView> deployedModuleViews) {
        if (deployedModuleViews != null) {
            return deployedModuleViews.stream().map(DeployedModuleOutput::new).collect(toList());
        }
        return null;
    }
}
