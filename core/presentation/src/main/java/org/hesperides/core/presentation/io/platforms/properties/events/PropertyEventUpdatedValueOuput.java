package org.hesperides.core.presentation.io.platforms.properties.events;

import javax.validation.constraints.NotNull;

import org.hesperides.core.domain.platforms.entities.properties.events.PropertyEventUpdatedValue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@AllArgsConstructor
public class PropertyEventUpdatedValueOuput {

    @NotNull
    String name;

    @SerializedName("old_value")
    @JsonProperty("old_value")
    @NotNull
    String oldValue;

    @SerializedName("new_value")
    @JsonProperty("new_value")
    @NotNull
    String newValue;

    public PropertyEventUpdatedValueOuput(PropertyEventUpdatedValue propertyEventUpdatedValue) {
        name = propertyEventUpdatedValue.getName();
        oldValue = propertyEventUpdatedValue.getOldValue();
        newValue = propertyEventUpdatedValue.getNewValue();
    }
}
