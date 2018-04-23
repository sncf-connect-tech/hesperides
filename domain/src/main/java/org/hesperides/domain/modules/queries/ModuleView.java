package org.hesperides.domain.modules.queries;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class ModuleView {
    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    @SerializedName("version_id")
    Long versionId;
}
