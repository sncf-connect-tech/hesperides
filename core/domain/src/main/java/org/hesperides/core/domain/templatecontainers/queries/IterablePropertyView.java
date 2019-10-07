package org.hesperides.core.domain.templatecontainers.queries;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterablePropertyView extends AbstractPropertyView {

    List<AbstractPropertyView> properties;

    public IterablePropertyView(String name, List<AbstractPropertyView> properties) {
        super(name);
        this.properties = properties;
    }

    @Override
    protected Stream<PropertyView> flattenProperties() {
        return Optional.ofNullable(properties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(AbstractPropertyView::flattenProperties)
                .flatMap(Function.identity());
    }
}
