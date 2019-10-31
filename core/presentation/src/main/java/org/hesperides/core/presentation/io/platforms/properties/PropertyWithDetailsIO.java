package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertyWithDetails;
import org.springframework.lang.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class PropertyWithDetailsIO {
    @Nullable
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
        return new PropertyWithDetails(this.name, this.storedValue, this.finalValue, this.defaultValue, this.transformations);
    }

    PropertyWithDetails toDomainInstanceWithBlankPropertiesToNull() {
        return new PropertyWithDetails(setTBlankPropertyTooNull(this.name), setTBlankPropertyTooNull(this.storedValue), setTBlankPropertyTooNull(this.finalValue), this.defaultValue, this.transformations);
    }

    String setTBlankPropertyTooNull(String property) {
        return StringUtils.isBlank(property) ? null : property;
    }

    public static List<PropertyWithDetails> toDomainInstance(List<PropertyWithDetailsIO> propertiesWithDetailsProvided, boolean withNullProperties) {

        List<PropertyWithDetails> propertyWithDetails = null;
        if (withNullProperties) {
            propertyWithDetails = propertiesWithDetailsProvided.stream().sorted(Comparator.comparing(PropertyWithDetailsIO::getName))
                    .map(propertyWithDetailsIO -> propertyWithDetailsIO.toDomainInstanceWithBlankPropertiesToNull())
                    .collect(Collectors.toList());
        } else {
            propertyWithDetails = propertiesWithDetailsProvided.stream().sorted(Comparator.comparing(PropertyWithDetailsIO::getName))
                    .map(propertyWithDetailsIO -> propertyWithDetailsIO.toDomainInstance())
                    .collect(Collectors.toList());
        }
        return propertyWithDetails;
    }

}
