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
        return new PropertyWithDetails(name, storedValue, finalValue, defaultValue, transformations);
    }

    // Only way i found to manage the null property values of cucumber Datatable
    PropertyWithDetails replaceBlankPropertiesWithNull() {
        return new PropertyWithDetails(replaceBlankPropertiesWithNull(name), replaceBlankPropertiesWithNull(storedValue), replaceBlankPropertiesWithNull(finalValue), defaultValue, transformations);
    }

    String replaceBlankPropertiesWithNull(String property) {
        return StringUtils.isBlank(property) ? null : property;
    }

    public static List<PropertyWithDetails> toDomainInstance(List<PropertyWithDetailsIO> propertiesWithDetailsProvided, boolean withNullAttributesProperties) {

        List<PropertyWithDetails> propertyWithDetails;
        if (withNullAttributesProperties) {
            propertyWithDetails = propertiesWithDetailsProvided.stream().sorted(Comparator.comparing(PropertyWithDetailsIO::getName))
                    .map(propertyWithDetailsIO -> propertyWithDetailsIO.replaceBlankPropertiesWithNull())
                    .collect(Collectors.toList());
        } else {
            propertyWithDetails = propertiesWithDetailsProvided.stream().sorted(Comparator.comparing(PropertyWithDetailsIO::getName))
                    .map(propertyWithDetailsIO -> propertyWithDetailsIO.toDomainInstance())
                    .collect(Collectors.toList());
        }
        return propertyWithDetails;
    }
}
