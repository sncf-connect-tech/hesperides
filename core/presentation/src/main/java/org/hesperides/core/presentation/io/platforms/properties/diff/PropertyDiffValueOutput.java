package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;

@Value
class PropertyDiffValueOutput {
    String finalValue;
    String defaultValue;
    String storedValue; // Correspond à la valeur en base / renseignée dans l'IHM

    PropertyDiffValueOutput(SimplePropertyVisitor propertyVisitor) {
        this.finalValue = propertyVisitor.getValue().get();
        this.defaultValue = propertyVisitor.getDefaultValue().orElse(null);
        this.storedValue = propertyVisitor.getInitialValue();
    }
}
