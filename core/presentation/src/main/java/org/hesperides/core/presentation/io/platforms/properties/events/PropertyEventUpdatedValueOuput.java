package org.hesperides.core.presentation.io.platforms.properties.events;

import org.hesperides.core.domain.platforms.entities.properties.events.PropertyEventUpdatedValue;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@AllArgsConstructor
public class PropertyEventUpdatedValueOuput {
    String name;
    String oldValue;
    String newValue;

    public PropertyEventUpdatedValueOuput(PropertyEventUpdatedValue propertyEventUpdatedValue) {
        name = propertyEventUpdatedValue.getName();
        oldValue = propertyEventUpdatedValue.getOldValue();
        newValue = propertyEventUpdatedValue.getNewValue();
    }
}
