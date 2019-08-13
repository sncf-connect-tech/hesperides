package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.NonNull;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;
import org.springframework.lang.Nullable;

@Value
class PropertyDiffValueOutput {
    @Nullable
    String finalValue;
    @Nullable
    String defaultValue;
    @Nullable
    String storedValue; // Correspond à la valeur en base / renseignée dans l'IHM
    @NonNull
    ValuedPropertyTransformation[] transformations;

    PropertyDiffValueOutput(SimplePropertyVisitor propertyVisitor) {
        if (propertyVisitor != null) {
            finalValue = propertyVisitor.getValueOrDefault().orElse(null);
            defaultValue = propertyVisitor.getDefaultValue().orElse(null);
            storedValue = propertyVisitor.getInitialValue();
            transformations = propertyVisitor.getTransformations();
        } else {
            finalValue = null;
            defaultValue = null;
            storedValue = null;
            transformations = new ValuedPropertyTransformation[]{};
        }
    }
}
