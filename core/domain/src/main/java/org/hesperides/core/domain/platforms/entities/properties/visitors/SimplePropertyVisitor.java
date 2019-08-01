package org.hesperides.core.domain.platforms.entities.properties.visitors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode
public class SimplePropertyVisitor implements PropertyVisitor {

    // Invariant: les 2 champs suivants ne sont jamais simultanément vide & null
    // Invariant: les attributs .propertyValue et tous les modèles dans .propertyModels ont le même nom
    private final List<PropertyView> propertyModels;
    private final ValuedPropertyView propertyValue;
    @Getter
    private final String initialValue;

    SimplePropertyVisitor(ValuedPropertyView propertyValue) {
        this(Collections.emptyList(), propertyValue, propertyValue.getValue());
    }

    public SimplePropertyVisitor(List<PropertyView> propertyModels,
                                 ValuedPropertyView propertyValue) {
        // propertyValue peut être null dans le cas où on construit un Visitor uniquement à partir du modèle
        this(propertyModels, propertyValue, propertyValue == null ? null : propertyValue.getValue());
    }

    private SimplePropertyVisitor(List<PropertyView> propertyModels,
                                  ValuedPropertyView propertyValue,
                                  String initialValue) {
        this.propertyModels = propertyModels;
        this.propertyValue = propertyValue;
        this.initialValue = initialValue;
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels,
                                                           ValuedPropertyView valuedPropertyView) {
        return new SimplePropertyVisitor(propertiesModels.stream()
                .filter(PropertyView.class::isInstance)
                .map(PropertyView.class::cast)
                .collect(Collectors.toList()), valuedPropertyView);
    }

    public boolean isValued() {
        return propertyValue != null && StringUtils.isNotBlank(propertyValue.getValue());
    }

    public Optional<String> getValue() {
        return (isValued()) ? Optional.of(propertyValue.getValue()) : getDefaultValue();
    }

    public Optional<String> getDefaultValue() {
        return (propertyModels.size() > 0) ? Optional.ofNullable(propertyModels.get(0).getDefaultValue()) : Optional.empty();
    }

    public Map<String, String> getMustacheKeyValues() {
        return propertyModels.stream().collect(Collectors.toMap(
                PropertyView::getMustacheContent,
                propertyModel -> propertyValue != null ? propertyValue.getValue() : getDefaultValue().get()
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
    public PropertyVisitor mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper) {
        return mapper.apply(this);
    }

    @Override
    public PropertyVisitor mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper) {
        return this;
    }

    @Override
    public boolean equals(PropertyVisitor propertyVisitor, boolean compareStoredValue) {
        boolean isEqual = false;
        if (getName().equals(propertyVisitor.getName()) && propertyVisitor instanceof SimplePropertyVisitor) {
            SimplePropertyVisitor visitor = (SimplePropertyVisitor) propertyVisitor;
            if (compareStoredValue) {
                isEqual = Objects.equals(getInitialValue(), visitor.getInitialValue());
            } else {
                isEqual = Objects.equals(getValue(), visitor.getValue());
            }
        }
        return isEqual;
    }

    public SimplePropertyVisitor withValue(String newValue) {
        ValuedPropertyView newValuedPropertyView;
        if (propertyValue != null) {
            newValuedPropertyView = propertyValue.withValue(newValue);
        } else {
            newValuedPropertyView = new ValuedPropertyView(propertyModels.get(0).getName(), newValue);
        }
        return new SimplePropertyVisitor(
                propertyModels,
                newValuedPropertyView,
                initialValue
        );
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels) {
        return fromAbstractPropertyViews(propertiesModels, null);
    }
}
