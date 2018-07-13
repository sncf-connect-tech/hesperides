package org.hesperides.core.domain.templatecontainers.queries;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
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
