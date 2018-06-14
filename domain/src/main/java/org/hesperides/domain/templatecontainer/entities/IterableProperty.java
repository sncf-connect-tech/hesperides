package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;

import java.util.List;

@Value
public class IterableProperty extends Property {

    List<Property> properties;
    List<IterableProperty> iterableProperties;

    public IterableProperty(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<Property> properties, List<IterableProperty> iterableProperties) {
        super(name, isRequired, comment, defaultValue, pattern, isPassword);
        this.properties = properties;
        this.iterableProperties = iterableProperties;
    }
}
