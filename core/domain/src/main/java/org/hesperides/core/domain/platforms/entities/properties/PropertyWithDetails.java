package org.hesperides.core.domain.platforms.entities.properties;

import lombok.Value;

@Value
public class PropertyWithDetails {
    String name;
    String storedValue;
    String finalValue;
    String defaultValue;
    ValuedPropertyTransformation[] transformations;
}
