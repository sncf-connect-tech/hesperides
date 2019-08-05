package org.hesperides.core.domain.platforms.entities.properties.visitors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import static java.util.stream.Collectors.toMap;

@Value
@Slf4j
public class PropertyVisitorsSequence {

    List<PropertyVisitor> properties;

    public PropertyVisitorsSequence(List<PropertyVisitor> properties) {
        this.properties = Collections.unmodifiableList(properties);
    }

    public static PropertyVisitorsSequence empty() {
        return new PropertyVisitorsSequence(Collections.emptyList());
    }

    public static PropertyVisitorsSequence fromModelAndValuedProperties(List<AbstractPropertyView> propertiesModels,
                                                                        List<AbstractValuedPropertyView> valuedProperties,
                                                                        boolean includePropertiesWithoutModel) {
        Map<String, List<AbstractPropertyView>> propertyModelsPerName = propertiesModels.stream().collect(groupingBy(AbstractPropertyView::getName));
        List<PropertyVisitor> propertyVisitors = valuedProperties.stream().map(valuedProperty -> {
            PropertyVisitor propertyVisitor = null;
            // Si des valorisations existent sans modèle de propriété correspondant,
            // cette lambda retourne un Optional.empty() qui sera exclu de la liste finale
            if (valuedProperty instanceof ValuedPropertyView) {
                if (propertyModelsPerName.containsKey(valuedProperty.getName())) {
                    propertyVisitor = SimplePropertyVisitor.fromAbstractPropertyViews(propertyModelsPerName.get(valuedProperty.getName()), (ValuedPropertyView) valuedProperty);
                } else if (includePropertiesWithoutModel) {
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


    // On concatène les propriétés parentes avec les propriété de l'item
    // pour bénéficier de la valorisation de ces propriétés dans les propriétés filles
    // cf. BDD Scenario: get file with an iterable-ception
    public PropertyVisitorsSequence passOverPropertyValuesToChildItems() {
        return this.mapSequencesRecursive(propertyVisitors -> {
            List<SimplePropertyVisitor> simpleSimplePropertyVisitors = propertyVisitors.getSimplePropertyVisitors();
            return propertyVisitors.mapDirectChildIterablePropertyVisitors(
                    iterablePropertyVisitor -> iterablePropertyVisitor.addPropertyVisitorsOrUpdateValue(simpleSimplePropertyVisitors)
            );
        });
    }

    private List<SimplePropertyVisitor> getSimplePropertyVisitors() {
        return properties.stream()
                .filter(SimplePropertyVisitor.class::isInstance)
                .map(SimplePropertyVisitor.class::cast)
                .collect(Collectors.toList());
    }

    // Juste avant d'appeler le moteur Mustache,
    // on supprime toutes les {{mustaches}} n'ayant pas déjà été substituées par `preparePropertiesValues` des valorisations,
    // afin que les fichiers générés n'en contiennent plus aucune trace
    public PropertyVisitorsSequence removeMustachesInPropertyValues() {
        return this.mapSimplesRecursive(propertyVisitor -> {
            if (propertyVisitor.isValued()) {
                propertyVisitor = propertyVisitor.withValue(StringUtils.removeAll(propertyVisitor.getValueOrDefault().get(), "\\{\\{[^}]*\\}\\}"));
            }
            return propertyVisitor;
        });
    }

    public PropertyVisitorsSequence addValuedPropertiesIfUndefined(Stream<ValuedPropertyView> extraProperties) {
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
                newProperties.set(matchingPropertyIndex, matchingSimplePropertyVisitor.withValue(extraVisitorProperty.getValueOrDefault().get()));
            }
        });
        return new PropertyVisitorsSequence(newProperties);
    }

    public PropertyVisitorsSequence addOverridingValuedProperties(List<ValuedPropertyView> extraValuedProperties) {
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

    public PropertyVisitorsSequence removePropertiesByName(Set<String> excludedPropertyNames) {
        return new PropertyVisitorsSequence(properties.stream()
                .filter(property -> !excludedPropertyNames.contains(property.getName()))
                .collect(Collectors.toList()));
    }

    /* Applique une fonction récursivement à toutes propriétés simple, sans provoquer de transformation */
    void forEachSimplesRecursive(Consumer<SimplePropertyVisitor> consumer) {
        properties.forEach(property -> property.acceptSimplesRecursive(consumer));
    }

    /* Applique une fonction récursivement, en transformant potentiellement les propriétés */
    public PropertyVisitorsSequence mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper) {
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

    private PropertyVisitorsSequence mapDirectChildIterablePropertyVisitors(Function<IterablePropertyVisitor, PropertyVisitor> mapper) {
        return new PropertyVisitorsSequence(properties.stream()
                .map(iPropertyVisitor -> {
                    if (iPropertyVisitor instanceof IterablePropertyVisitor) {
                        iPropertyVisitor = mapper.apply((IterablePropertyVisitor) iPropertyVisitor);
                    }
                    return iPropertyVisitor;
                }).collect(Collectors.toList()));
    }

    public Stream<PropertyVisitor> stream() {
        return properties.stream();
    }

    public boolean equals(PropertyVisitorsSequence otherSequence, boolean compareStoredValues) {
        Map<String, PropertyVisitor> propertyVisitorMap = properties.stream().collect(toMap(PropertyVisitor::getName, property -> property));
        return (properties.size() == otherSequence.getProperties().size()) && otherSequence.properties.stream().allMatch(p -> p.equals(propertyVisitorMap.get(p.getName()), compareStoredValues));
    }
}
