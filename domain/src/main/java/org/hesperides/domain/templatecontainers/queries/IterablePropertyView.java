package org.hesperides.domain.templatecontainers.queries;

import lombok.Value;

import java.util.List;

@Value
public class IterablePropertyView extends AbstractPropertyView {

    List<AbstractPropertyView> properties;

    public IterablePropertyView(String name, List<AbstractPropertyView> properties) {
        super(name);
        this.properties = properties;
    }
}
