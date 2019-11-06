package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Value
public class PropertyWithDetailsIO {

    @NonNull
    String name;
    @Nullable
    String storedValue;
    @Nullable
    String finalValue;
    @Nullable
    String defaultValue;
    @Nullable
    ValuedPropertyTransformation[] transformations;

    PropertyWithDetails toDomainInstance() {
        return new PropertyWithDetails(getName(), storedValue, finalValue, defaultValue, transformations);
    }
}
