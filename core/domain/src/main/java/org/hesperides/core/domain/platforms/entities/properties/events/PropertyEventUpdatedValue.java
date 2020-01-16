package org.hesperides.core.domain.platforms.entities.properties.events;


import lombok.Value;

@Value
public class PropertyEventUpdatedValue {
    String name;
    String oldValue;
    String newValue;
}
