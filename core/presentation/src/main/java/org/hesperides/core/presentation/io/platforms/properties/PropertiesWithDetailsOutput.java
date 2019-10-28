package org.hesperides.core.presentation.io.platforms.properties;

import lombok.NonNull;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.springframework.lang.Nullable;

@Value
public class PropertiesWithDetailsOutput {
    @Nullable
    String finalValue;
    @Nullable
    String defaultValue;
    @Nullable
    String storedValue; // Correspond à la valeur en base / renseignée dans l'IHM
    @NonNull
    ValuedPropertyTransformation[] transformations;
}
