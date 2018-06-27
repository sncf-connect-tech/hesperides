package org.hesperides.domain.templatecontainers.queries;

import lombok.Value;

@Value
public class PropertyView extends AbstractPropertyView {

    boolean required;
    String comment;
    String defaultValue;
    String pattern;
    boolean password;

    public PropertyView(String name, boolean required, String comment, String defaultValue, String pattern, boolean password) {
        super(name);
        this.required = required;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
        this.password = password;
    }
}
