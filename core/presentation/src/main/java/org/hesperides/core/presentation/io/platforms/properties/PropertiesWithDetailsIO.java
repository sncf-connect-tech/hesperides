package org.hesperides.core.presentation.io.platforms.properties;

import org.hesperides.core.domain.platforms.entities.properties.diff.PropertyWithDetails;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class PropertiesWithDetailsIO extends PropertiesIO<PropertyWithDetails> {
    public PropertiesWithDetailsIO(@Valid Long propertiesVersionId, @NotNull @Valid Set<PropertyWithDetails> valuedProperties, @NotNull @Valid Set<IterableValuedPropertyIO> iterableValuedProperties) {
        super(propertiesVersionId, valuedProperties, iterableValuedProperties);
    }

}
