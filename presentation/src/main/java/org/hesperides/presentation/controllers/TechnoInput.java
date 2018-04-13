package org.hesperides.presentation.controllers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class TechnoInput {
    @NotNull
    String name;
    @NotNull
    String filename;
    @NotNull
    String location;
    @NotNull
    String content;
    @NotNull
    @SerializedName("version_id")
    Long versionId;
    @NotNull
    RightsInput rights;
}
