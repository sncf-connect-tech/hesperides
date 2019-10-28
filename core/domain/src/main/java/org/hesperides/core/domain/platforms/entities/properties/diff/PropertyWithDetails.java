package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;

@Value
public class PropertyWithDetails {
    String name;
    String storedValue;
    String finalValue;
    String defaultValue;

    ValuedPropertyTransformation[] transformations;
}
