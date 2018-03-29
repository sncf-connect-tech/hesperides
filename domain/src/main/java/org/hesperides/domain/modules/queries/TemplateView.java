package org.hesperides.domain.modules.queries;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class TemplateView {
    String name;
    String namespace;
    String filename;
    String location;
    @SerializedName("version_id")
    Long versionId;
}
