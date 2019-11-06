package org.hesperides.core.domain.platforms.entities.properties.visitors;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimplePropertyVisitor implements PropertyVisitor {

    // Invariant: les 2 champs suivants ne sont jamais simultanément vide & null
    // Invariant: les attributs .propertyValue et tous les modèles dans .propertyModels ont le même nom
    private final List<PropertyView> propertyModels;
    private final ValuedPropertyView propertyValue;
    @Getter
    private final String initialValue;
    private final ValuedPropertyTransformation[] transformations;

    SimplePropertyVisitor(ValuedPropertyView propertyValue) {
        this(Collections.emptyList(), propertyValue, propertyValue.getValue(), new ValuedPropertyTransformation[]{});
    }

    public SimplePropertyVisitor(List<PropertyView> propertyModels,
                                 ValuedPropertyView propertyValue) {
        // propertyValue peut être null dans le cas où on construit un Visitor uniquement à partir du modèle
        this(propertyModels, propertyValue, propertyValue == null ? null : propertyValue.getValue(), new ValuedPropertyTransformation[]{});
    }

    private SimplePropertyVisitor(List<PropertyView> propertyModels,
                                  ValuedPropertyView propertyValue,
                                  String initialValue,
                                  ValuedPropertyTransformation[] transformations) {
        this.propertyModels = propertyModels;
        this.propertyValue = propertyValue;
        this.initialValue = initialValue;
        this.transformations = transformations;
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels,
                                                           ValuedPropertyView valuedPropertyView) {
        return new SimplePropertyVisitor(propertiesModels.stream()
                .filter(PropertyView.class::isInstance)
                .map(PropertyView.class::cast)
                .collect(Collectors.toList()), valuedPropertyView);
    }

    @Override
    public boolean isValued() {
        return propertyValue != null && StringUtils.isNotBlank(propertyValue.getValue());
    }

    public Optional<String> getValueOrDefault() {
        return isValued() ? Optional.ofNullable(propertyValue.getValue()) : getDefaultValue();
    }

    public Optional<String> getDefaultValue() {
        return CollectionUtils.isEmpty(propertyModels) ? Optional.empty() : Optional.ofNullable(propertyModels.get(0).getDefaultValue());
    }

    public Map<String, String> getMustacheKeyValues() {
        Map<String, String> keyValues = new HashMap<>();
        for (PropertyView propertyModel : propertyModels) {
            if (isValued()) {
                keyValues.put(propertyModel.getMustacheContent(), propertyValue.getValue());
            } else if (getDefaultValue().isPresent()) {
                keyValues.put(propertyModel.getMustacheContent(), getDefaultValue().get());
            }
        }
        return keyValues;
    }

    @Override
    public String getName() {
        if (propertyValue != null) {
            return propertyValue.getName();
        }
        return propertyModels.get(0).getName();
    }

    public ValuedPropertyTransformation[] getTransformations() {
        return transformations;
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
    public List<PropertyWithDetails> getPropertiesWithDetails() {
        List<PropertyWithDetails> propertyWithDetails;
        if (propertyValue != null) {
            propertyWithDetails = propertyModels.stream()
                    .map(property -> new PropertyWithDetails(getName(), initialValue, propertyValue.getValue(), property.getDefaultValue(), transformations))
                    .collect(Collectors.toList());
        } else {
            propertyWithDetails = propertyModels.stream()
                    .map(property -> new PropertyWithDetails(getName(), initialValue, null, property.getDefaultValue(), transformations))
                    .collect(Collectors.toList());
        }
        return propertyWithDetails;
    }

    @Override
    public boolean equals(PropertyVisitor propertyVisitor, boolean compareStoredValue) {
        boolean isEqual = false;
        if (getName().equals(propertyVisitor.getName()) && propertyVisitor instanceof SimplePropertyVisitor) {
            SimplePropertyVisitor visitor = (SimplePropertyVisitor) propertyVisitor;
            if (compareStoredValue) {
                isEqual = Objects.equals(getInitialValue(), visitor.getInitialValue());
            } else {
                isEqual = Objects.equals(getValueOrDefault(), visitor.getValueOrDefault());
            }
        }
        return isEqual;
    }

    public SimplePropertyVisitor withValue(String newValue, ValuedPropertyTransformation transformation) {
        ValuedPropertyView newValuedPropertyView;
        if (propertyValue != null) {
            newValuedPropertyView = propertyValue.withValue(newValue);
        } else {
            newValuedPropertyView = new ValuedPropertyView(propertyModels.get(0).getName(), newValue);
        }
        ValuedPropertyTransformation[] newTransformations = Arrays.copyOf(transformations, transformations.length + 1);
        newTransformations[transformations.length] = transformation;
        return new SimplePropertyVisitor(
                propertyModels,
                newValuedPropertyView,
                initialValue,
                newTransformations
        );
    }

    static SimplePropertyVisitor fromAbstractPropertyViews(List<AbstractPropertyView> propertiesModels) {
        return fromAbstractPropertyViews(propertiesModels, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePropertyVisitor that = (SimplePropertyVisitor) o;
        return Objects.equals(propertyModels, that.propertyModels) &&
                Objects.equals(propertyValue, that.propertyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyModels, propertyValue);
    }
}
