package org.hesperides.core.application.files;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Value
@Slf4j
class PropertyVisitorsSequence {
    List<PropertyVisitor> properties;

    PropertyVisitorsSequence(List<PropertyVisitor> properties) {
        this.properties = Collections.unmodifiableList(properties);
    }

    int size() {
        return properties.size();
    }

    List<SimplePropertyVisitor> getSimplePropertyVisitors() {
        return properties.stream()
                .filter(SimplePropertyVisitor.class::isInstance)
                .map(SimplePropertyVisitor.class::cast)
                .collect(Collectors.toList());
    }

    static PropertyVisitorsSequence fromModelAndValuedProperties(List<AbstractPropertyView> propertyModels,
                                                                 List<AbstractValuedPropertyView> valuedProperties) {
        return fromModelAndValuedProperties(propertyModels, valuedProperties, true);
    }

    static PropertyVisitorsSequence fromModelAndValuedProperties(List<AbstractPropertyView> propertiesModels,
                                                                 List<AbstractValuedPropertyView> valuedProperties,
                                                                 boolean filterOutValuedPropertiesWithoutModel) {
        Map<String, List<AbstractPropertyView>> propertyModelsPerName = propertiesModels.stream().collect(groupingBy(AbstractPropertyView::getName));
        List<PropertyVisitor> propertyVisitors = valuedProperties.stream().map(valuedProperty -> {
            PropertyVisitor propertyVisitor = null;
            // Si des valorisations existent sans modèle de propriété correspondant,
            // cette lambda retourne un Optional.empty() qui sera exclue de la liste finale
            if (valuedProperty instanceof ValuedPropertyView) {
                if (propertyModelsPerName.containsKey(valuedProperty.getName())) {
                    propertyVisitor = SimplePropertyVisitor.fromAbstractPropertyViews(propertyModelsPerName.get(valuedProperty.getName()), (ValuedPropertyView) valuedProperty);
                } else if (!filterOutValuedPropertiesWithoutModel) {
                    propertyVisitor = new SimplePropertyVisitor((ValuedPropertyView) valuedProperty);
                }
            } else if (valuedProperty instanceof IterableValuedPropertyView && propertyModelsPerName.containsKey(valuedProperty.getName())) {
                // L'appel à `groupingBy` ci-dessus nous assure qu'il y a toujours au moins un élement dans la liste de modèles de propriétés.
                // Dans le cas des itérables, comme ils ne sont jamais employés avec des annotations post-pipe,
                // un unique modèle existe toujours
                IterablePropertyView iterablePropertyModel = (IterablePropertyView) propertyModelsPerName.get(valuedProperty.getName()).get(0);
                propertyVisitor = new IterablePropertyVisitor(iterablePropertyModel, (IterableValuedPropertyView) valuedProperty);
            }
            return Optional.ofNullable(propertyVisitor);
        }).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
        // Maintenant on ajoute les propriétés pour lesquelles on a un modèle mais pas de valorisation
        Set<String> valuedPropertyNames = propertyVisitors.stream().map(PropertyVisitor::getName).collect(Collectors.toSet());
        propertyModelsPerName.forEach((propertyName, propertyModels) -> {
            if (!valuedPropertyNames.contains(propertyName)) {
                AbstractPropertyView firstPropertyView = propertyModels.get(0);
                if (firstPropertyView instanceof PropertyView) {
                    propertyVisitors.add(SimplePropertyVisitor.fromAbstractPropertyViews(propertyModels));
                } else {
                    propertyVisitors.add(new IterablePropertyVisitor(propertyModels));
                }
            }
        });
        return new PropertyVisitorsSequence(propertyVisitors);
    }

    PropertyVisitorsSequence addValuedPropertiesIfUndefined(Stream<ValuedPropertyView> extraProperties) {
        Map<String, Integer> indexPerPropertyName = buildIndexPerPropertyName();
        List<PropertyVisitor> newProperties = new ArrayList<>(properties);
        extraProperties.forEach(valuedProperty -> {
            Integer matchingPropertyIndex = indexPerPropertyName.get(valuedProperty.getName());
            if (matchingPropertyIndex == null) {
                newProperties.add(new SimplePropertyVisitor(valuedProperty));
            }
        });
        return new PropertyVisitorsSequence(newProperties);
    }

    PropertyVisitorsSequence addPropertyVisitorsOrUpdateValue(List<SimplePropertyVisitor> extraProperties) {
        Map<String, Integer> indexPerPropertyName = buildIndexPerPropertyName();
        List<PropertyVisitor> newProperties = new ArrayList<>(properties);
        extraProperties.forEach(extraVisitorProperty -> {
            Integer matchingPropertyIndex = indexPerPropertyName.get(extraVisitorProperty.getName());
            if (matchingPropertyIndex == null) {
                newProperties.add(extraVisitorProperty);
                return;
            }
            if (!(properties.get(matchingPropertyIndex) instanceof SimplePropertyVisitor)) {
                return;
            }
            SimplePropertyVisitor matchingSimplePropertyVisitor = (SimplePropertyVisitor) properties.get(matchingPropertyIndex);
            if (!matchingSimplePropertyVisitor.isValued() && extraVisitorProperty.isValued()) {
                newProperties.set(matchingPropertyIndex, matchingSimplePropertyVisitor.withValue(extraVisitorProperty.getValue().get()));
            }
        });
        return new PropertyVisitorsSequence(newProperties);
    }

    PropertyVisitorsSequence addOverridingValuedProperties(List<ValuedPropertyView> extraValuedProperties) {
        Map<String, Integer> indexPerPropertyName = buildIndexPerPropertyName();
        List<PropertyVisitor> newProperties = new ArrayList<>(properties);
        for (ValuedPropertyView valuedProperty : extraValuedProperties) {
            Integer matchingPropertyIndex = indexPerPropertyName.get(valuedProperty.getName());
            if (matchingPropertyIndex != null) {
                PropertyVisitor propertyVisitor = newProperties.get(matchingPropertyIndex);
                if (propertyVisitor instanceof SimplePropertyVisitor) {
                    // On préserve le modèle de propriété
                    // cf. BDD Scenario: get file with instance properties created by a module property that references itself
                    newProperties.set(matchingPropertyIndex, ((SimplePropertyVisitor) propertyVisitor).withValue(valuedProperty.getValue()));
                }
            } else {
                newProperties.add(new SimplePropertyVisitor(valuedProperty));
            }
        }
        return new PropertyVisitorsSequence(newProperties);
    }

    private Map<String, Integer> buildIndexPerPropertyName() {
        Map<String, Integer> indexPerPropertyName = new HashMap<>();
        IntStream.range(0, properties.size())
                .forEach(i -> indexPerPropertyName.put(properties.get(i).getName(), i));
        return indexPerPropertyName;
    }

    /* Applique une fonction récursivement à toutes propriétés, sans provoquer de transformation */
    void forEach(Consumer<SimplePropertyVisitor> simpleConsumer, Consumer<IterablePropertyVisitor> iterableConsumer) {
        properties.forEach(property -> property.acceptEither(simpleConsumer, iterableConsumer));
    }

    /* Applique une fonction récursivement à toutes propriétés simple, sans provoquer de transformation */
    void forEachSimplesRecursive(Consumer<SimplePropertyVisitor> consumer) {
        properties.forEach(property -> property.acceptSimplesRecursive(consumer));
    }

    /* Applique une fonction récursivement, en transformant potentiellement les propriétés */
    PropertyVisitorsSequence mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper) {
        return new PropertyVisitorsSequence(
                properties.stream().map(property -> property.mapSimplesRecursive(mapper)).collect(Collectors.toList())
        );
    }

    /* Applique récursivement une fonction sur les propriétés itérables uniquement, en les transformant potentiellement */
    PropertyVisitorsSequence mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper) {
        return new PropertyVisitorsSequence(
                mapper.apply(this).properties.stream().map(property -> property.mapSequencesRecursive(mapper)).collect(Collectors.toList())
        );
    }

    PropertyVisitorsSequence mapDirectChildIterablePropertyVisitors(Function<IterablePropertyVisitor, PropertyVisitor> mapper) {
        return new PropertyVisitorsSequence(properties.stream()
                .map(iPropertyVisitor -> {
                    if (iPropertyVisitor instanceof IterablePropertyVisitor) {
                        iPropertyVisitor = mapper.apply((IterablePropertyVisitor) iPropertyVisitor);
                    }
                    return iPropertyVisitor;
                }).collect(Collectors.toList()));
    }
}
