package org.hesperides.core.presentation.io.platforms.properties;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    // Only way i found to manage the null property values of cucumber Datatable
    PropertyWithDetailsIO replaceBlankPropertiesWithNull() {
        return new PropertyWithDetailsIO(replaceBlankPropertiesWithNull(name), replaceBlankPropertiesWithNull(storedValue), replaceBlankPropertiesWithNull(finalValue), defaultValue, transformations);
    }

    public static List<PropertyWithDetailsIO> replaceBlankPropertiesWithNull(List<PropertyWithDetailsIO> providedPropertyWithDetails) {
       return providedPropertyWithDetails.stream()
               .map(PropertyWithDetailsIO::replaceBlankPropertiesWithNull)
               .collect(Collectors.toList());
    }

    String replaceBlankPropertiesWithNull(String property) {
        return StringUtils.isBlank(property) ? null : property;
    }
}
