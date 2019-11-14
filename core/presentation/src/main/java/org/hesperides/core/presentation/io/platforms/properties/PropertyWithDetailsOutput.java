package org.hesperides.core.presentation.io.platforms.properties;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.queries.views.properties.PropertyWithDetailsView;

@Value
@AllArgsConstructor
public class PropertyWithDetailsOutput {

    String name;
    String storedValue;
    String finalValue;
    String defaultValue;
    ValuedPropertyTransformation[] transformations;

    PropertyWithDetailsOutput(PropertyWithDetailsView propertyWithDetailsView) {
        name = propertyWithDetailsView.getName();
        storedValue = propertyWithDetailsView.getStoredValue();
        finalValue = propertyWithDetailsView.getFinalValue();
        defaultValue = propertyWithDetailsView.getDefaultValue();
        transformations = propertyWithDetailsView.getTransformations();
    }
}
