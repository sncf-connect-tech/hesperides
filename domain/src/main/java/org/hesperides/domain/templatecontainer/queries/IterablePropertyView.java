package org.hesperides.domain.templatecontainer.queries;

import lombok.Value;

import java.util.List;

@Value
public class IterablePropertyView extends PropertyView {

    List<PropertyView> properties;

    public IterablePropertyView(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<PropertyView> properties) {
        super(name, isRequired, comment, defaultValue, pattern, isPassword);
        this.properties = properties;
    }
}
