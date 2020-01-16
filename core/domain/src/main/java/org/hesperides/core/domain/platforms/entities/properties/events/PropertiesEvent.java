package org.hesperides.core.domain.platforms.entities.properties.events;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@AllArgsConstructor
public class PropertiesEvent {
    String author;
    String comment;
    Instant timestamp;
    List<String> addedProperties;
    List<String> removedProperties;
    List<PropertyEventUpdatedValue> updatedProperties;
}
