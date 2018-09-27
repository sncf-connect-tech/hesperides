package org.hesperides.core.domain.platforms.queries.views;

import lombok.Value;

import java.util.List;

@Value
public class InstanceModelView {
    List<InstanceModelPropertyView> instanceProperties;

    @Value
    public static class InstanceModelPropertyView {
        String name;
        String comment;
        boolean isRequired;
        String defaultValue;
        String pattern;
        boolean isPassword;
    }
}
