package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.Value;
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

    PropertyDiffValueOutput(SimplePropertyVisitor propertyVisitor) {
        if (propertyVisitor != null) {
            finalValue = propertyVisitor.getValueOrDefault().orElse(null);
            defaultValue = propertyVisitor.getDefaultValue().orElse(null);
            storedValue = propertyVisitor.getInitialValue();
        } else {
            finalValue = null;
            defaultValue = null;
            storedValue = null;
        }
    }
}
