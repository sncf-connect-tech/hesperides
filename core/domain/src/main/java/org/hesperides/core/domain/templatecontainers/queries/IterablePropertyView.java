package org.hesperides.core.domain.templatecontainers.queries;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterablePropertyView extends AbstractPropertyView {

    List<AbstractPropertyView> properties;

    public IterablePropertyView(String name, List<AbstractPropertyView> properties) {
        super(name);
        this.properties = properties;
    }
}
