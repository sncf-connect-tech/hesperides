package org.hesperides.core.domain.platforms.queries.views.properties;

import static org.hesperides.core.domain.platforms.entities.properties.ValuedProperty.streamValuesBetweenCurlyBrackets;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;

import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class PropertyReferenceScanner {

    public Set<String> findAll(List<AbstractValuedPropertyView> moduleProperties, List<InstanceView> instances) {
        final Stream<String> inModule = moduleProperties.stream()
                .flatMap(this::scan);

        final Stream<String> inInstances = instances.stream()
                .flatMap(instance -> instance.getValuedProperties().stream())
                .flatMap(this::fromSimple);

        return Stream.concat(inModule, inInstances).collect(toSet());
    }

    private Stream<String> fromIterable(IterableValuedPropertyView iterable) {
        return iterable.getIterablePropertyItems().stream()
                .flatMap(item -> item.getAbstractValuedPropertyViews().stream())
                .flatMap(this::scan); // récursion, mon amour ;)
    }

    private Stream<String> fromSimple(ValuedPropertyView simple) {
        return streamValuesBetweenCurlyBrackets(simple.getValue());
    }

    private Stream<String> scan(AbstractValuedPropertyView valuedProperty) {
        final Stream<String> references;

        if (valuedProperty instanceof ValuedPropertyView) {
            references = fromSimple((ValuedPropertyView) valuedProperty);

        } else if (valuedProperty instanceof IterableValuedPropertyView) {
            references = fromIterable((IterableValuedPropertyView) valuedProperty);

        } else {
            references = Stream.empty();
        }

        return references;
    }
}
