package org.hesperides.core.application.files;

import lombok.EqualsAndHashCode;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;

@EqualsAndHashCode
class SimplePropertyVisitor implements PropertyVisitor {

    // Invariant: les 2 champs suivants ne sont jamais simultanément vide & null
    private final List<PropertyView> propertyModels;
    private final ValuedPropertyView propertyValue;

    public boolean isValued() {
        return propertyValue != null;
    }

    Optional<String> getValue() {
        if (propertyValue != null) {
            return Optional.of(propertyValue.getValue());
        }
        return getDefaultValue();
    }

    private Optional<String> getDefaultValue() {
        if (propertyModels.size() > 0) {
            return Optional.ofNullable(propertyModels.get(0).getDefaultValue());
        }
        return Optional.empty();
    }

    Map<String, String> getMustacheKeyValues() {
        return propertyModels.stream().collect(Collectors.toMap(
                PropertyView::getMustacheContent,
                pm -> propertyValue != null ? propertyValue.getValue() : pm.getDefaultValue()
        ));
    }

    @Override
    public String getName() {
        if (propertyValue != null) {
            return propertyValue.getName();
        }
        return propertyModels.get(0).getName();
    }

    @Override
    public void acceptEither(Consumer<SimplePropertyVisitor> simpleConsumer, Consumer<IterablePropertyVisitor> iterableConsumer) {
        simpleConsumer.accept(this);
    }

    @Override
    public void acceptSimplesRecursive(Consumer<SimplePropertyVisitor> consumer) {
        consumer.accept(this);
    }

    @Override
    public boolean testSimplesRecursive(Predicate<SimplePropertyVisitor> predicate) {
        return predicate.test(this);
    }

    @Override
    public PropertyVisitor mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper) {
        return mapper.apply(this);
    }

    @Override
    public PropertyVisitor mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper) {
        return this;
    }

    SimplePropertyVisitor withValue(String newValue) {
        ValuedPropertyView newValuedPropertyView;
        if (propertyValue != null) {
            newValuedPropertyView = propertyValue.withValue(newValue);
        } else {
            newValuedPropertyView = new ValuedPropertyView(propertyModels.get(0).getName(), newValue);
        }
        return new SimplePropertyVisitor(
                propertyModels,
                newValuedPropertyView
        );
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels) {
        return fromAbstractPropertyViews(propertiesModels, null);
    }

    SimplePropertyVisitor(ValuedPropertyView propertyValue) {
        this(Collections.emptyList(), propertyValue);
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels,
                                                           ValuedPropertyView valuedPropertyView) {
        return new SimplePropertyVisitor(propertiesModels.stream()
                .map(PropertyView.class::cast)
                .collect(Collectors.toList()), valuedPropertyView);
    }

    private SimplePropertyVisitor(List<PropertyView> propertyModels,
                                  ValuedPropertyView propertyValue) {
        this.propertyModels = propertyModels;
        this.propertyValue = propertyValue;
    }
}
