package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class GlobalPropertyUsageOutput {
    boolean inModel;
    @SerializedName("path")
    @JsonProperty("path")
    String propertiesPath;
}
