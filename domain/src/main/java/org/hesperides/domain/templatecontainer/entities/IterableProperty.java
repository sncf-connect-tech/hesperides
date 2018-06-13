package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;

import java.util.List;

@Value
public class IterableProperty extends Property {

    List<Property> properties;

    public IterableProperty(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<Property> properties) {
        super(name, isRequired, comment, defaultValue, pattern, isPassword);
        this.properties = properties;
    }
}
