package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class GlobalPropertyUsageOutput {

    // On conserve `inModel` pour être rétrocompatible mais le nom
    // de cette propriété n'est pas pertinent.
    boolean inModel;

    // `isRemovedFromTemplate` est fonctionnellement
    // l'inverse d'`inModel`, mais plus parlant.
//    boolean isRemovedFromTemplate;

    @SerializedName("path")
    @JsonProperty("path")
    String propertiesPath;
}
