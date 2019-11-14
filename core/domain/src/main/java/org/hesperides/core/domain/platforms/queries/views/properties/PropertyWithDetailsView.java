package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;

@Value
public class PropertyWithDetailsView {
    String name;
    String storedValue;
    String finalValue;
    String defaultValue;
    ValuedPropertyTransformation[] transformations;
}
