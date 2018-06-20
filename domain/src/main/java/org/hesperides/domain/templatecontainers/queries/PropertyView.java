package org.hesperides.domain.templatecontainers.queries;

import lombok.Value;

@Value
public class PropertyView extends AbstractPropertyView {

    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;

    public PropertyView(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword) {
        super(name);
        this.isRequired = isRequired;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
        this.isPassword = isPassword;
    }
}
