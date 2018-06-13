package org.hesperides.domain.templatecontainer.queries;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class PropertyView {
    String name;
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;
}
