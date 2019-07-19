package org.hesperides.core.domain.platforms.entities.properties.visitors;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence.fromModelAndValuedProperties;

@Value
public class IterablePropertyVisitor implements PropertyVisitor {

    private String name;
    private List<PropertyVisitorsSequence> items;

    public IterablePropertyVisitor(List<AbstractPropertyView> propertyModels) {
        this(
                propertyModels.get(0).getName(),
                Collections.emptyList()
        );
    }

    IterablePropertyVisitor(IterablePropertyView iterablePropertyModel,
                            IterableValuedPropertyView iterableValuedProperty) {
        this(
                iterablePropertyModel.getName(),
                iterableValuedProperty.getIterablePropertyItems().stream()
                        .map(valuedPropertyItem -> fromModelAndValuedProperties(
                                iterablePropertyModel.getProperties(),
                                valuedPropertyItem.getAbstractValuedPropertyViews(),
                                // cf. BDD Scenario: get file with an iterable-ception
                                false
                                )
                        ).collect(Collectors.toList())
        );
    }

    public IterablePropertyVisitor(String name, List<PropertyVisitorsSequence> items) {
        this.name = name;
        this.items = Collections.unmodifiableList(items);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void acceptSimplesRecursive(Consumer<SimplePropertyVisitor> consumer) {
        items.forEach(item -> item.forEachSimplesRecursive(consumer));
    }

    @Override
    public void acceptEither(Consumer<SimplePropertyVisitor> simpleConsumer, Consumer<IterablePropertyVisitor> iterableConsumer) {
        iterableConsumer.accept(this);
    }

    @Override
    public PropertyVisitor mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper) {
        return new IterablePropertyVisitor(name, items.stream()
                .map(item -> item.mapSimplesRecursive(mapper))
                .collect(Collectors.toList()));
    }

    @Override
    public PropertyVisitor mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper) {
        return new IterablePropertyVisitor(name, items.stream()
                .map(item -> item.mapSequencesRecursive(mapper))
                .collect(Collectors.toList()));
    }

    public IterablePropertyVisitor addPropertyVisitorsOrUpdateValue(List<SimplePropertyVisitor> extraProperties) {
        return new IterablePropertyVisitor(name, items.stream()
                .map(item -> item.addPropertyVisitorsOrUpdateValue(extraProperties))
                .collect(Collectors.toList()));
    }
}
